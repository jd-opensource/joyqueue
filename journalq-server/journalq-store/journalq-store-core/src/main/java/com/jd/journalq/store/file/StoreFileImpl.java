package com.jd.journalq.store.file;

import com.jd.journalq.store.PartialLogException;
import com.jd.journalq.store.ReadException;
import com.jd.journalq.store.utils.PreloadBufferPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

/**
 * 支持并发、带缓存页、顺序写入的文件
 */
public class StoreFileImpl<T> implements StoreFile<T> {
    private static final Logger logger = LoggerFactory.getLogger(StoreFileImpl.class);
    // 文件全局位置
    private final long filePosition;
    // 文件头长度
    private final int headerSize;
    // 对应的File
    private final File file;
    // buffer读写锁：
    // 访问(包括读和写）buffer时加读锁；
    // 加载、释放buffer时加写锁；
    private  final StampedLock bufferLock = new StampedLock();
    // 缓存页
    private ByteBuffer pageBuffer = null;

    // 缓存页类型
    // 只读：
    // MAPPED_BUFFER：mmap映射内存镜像文件；
    // 读写：
    // DIRECT_BUFFER: 数据先写入DirectBuffer，异步刷盘到文件，性能最好；
    // WRITE_MAP：数据保存到Map中，异步刷盘到文件，性能稍差，节省内存；
    private static final int MAPPED_BUFFER = 0, DIRECT_BUFFER = 1, WRITE_MAP = 2, NO_BUFFER = -1;
    private int bufferType = NO_BUFFER;

    private PreloadBufferPool bufferPool;
    private int capacity;
    private long lastAccessTime = System.currentTimeMillis();

    // 当前刷盘位置
    private int flushPosition;
    // 当前写入位置
    private int writePosition = 0;

    private final LogSerializer<T> serializer;
    private final Map<Integer, T> writeMap = new ConcurrentHashMap<>();
    // 读写文件缓存的长度，至少要超过最大消息的长度
    private final int bufferLength;
    private long timestamp = -1L;


    public StoreFileImpl(long filePosition, File base, int headerSize, LogSerializer<T> serializer, PreloadBufferPool bufferPool, int maxFileDataLength, int bufferLength) {
        this.filePosition = filePosition;
        this.headerSize = headerSize;
        this.serializer = serializer;
        this.bufferPool = bufferPool;
        this.capacity = maxFileDataLength;
        this.bufferLength = bufferLength;
        this.file = new File(base, String.valueOf(filePosition));
        if(file.exists() && file.length() > headerSize) {
            this.writePosition = (int)(file.length() - headerSize);
            this.flushPosition = writePosition;
        }
    }


    @Override
    public File file() {
        return file;
    }

    @Override
    public long position() {
        return filePosition;
    }

    private void loadRoUnsafe() throws IOException{
            if (null != pageBuffer) throw new IOException("Buffer already loaded!");
            ByteBuffer loadBuffer;
            try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
                loadBuffer =
                        fileChannel.map(FileChannel.MapMode.READ_ONLY, headerSize, file.length() - headerSize);
            }
            pageBuffer = loadBuffer;
            bufferType = MAPPED_BUFFER;
            pageBuffer.clear();
    }

    private void loadRwUnsafe() throws IOException{
            if(bufferType == DIRECT_BUFFER || bufferType == WRITE_MAP) {
                return;
            } else if(bufferType == MAPPED_BUFFER) {
                unloadUnsafe();
            }
            try {
                ByteBuffer buffer = bufferPool.allocate(capacity);
                loadDirectBuffer(buffer);
            } catch (OutOfMemoryError oom) {
                logger.warn("Insufficient direct memory, use write map instead. File: {}", file.getAbsolutePath());
                loadWriteMap();
            }
    }

    private void loadDirectBuffer(ByteBuffer buffer) throws IOException {
        if (file.exists() && file.length() > headerSize) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
                fileChannel.position(headerSize);
                int length;
                do {
                    length = fileChannel.read(buffer);
                } while (length > 0);
            }
            buffer.clear();
        }
        this.pageBuffer = buffer;
        bufferType = DIRECT_BUFFER;
    }


    private void loadWriteMap() throws IOException {

        int position = 0;
        if (file.exists() && file.length() > headerSize) {
            ByteBuffer writeBuffer = bufferPool.allocate(bufferLength);

            try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
                fileChannel.position(headerSize);
                int length;
                do {
                    writeBuffer.clear();
                    length = fileChannel.read(writeBuffer);
                    writeBuffer.flip();
                    while (writeBuffer.hasRemaining()) {
                        try {
                            T t = serializer.read(writeBuffer, -1);
                            int sizeOfT = serializer.size(t);
                            writeMap.put(position, t);
                            position += sizeOfT;
                        }catch (PartialLogException e) {
                            // 处理最后剩余半条消息的情况
                            fileChannel.position(fileChannel.position() - writeBuffer.remaining());
                            break;
                        }
                    }
                } while (length > 0);

                if(position < writePosition) {
                    writePosition = position;
                    if(writePosition < flushPosition) {
                        flushPosition = writePosition;
                    }
                }
            }finally {
                bufferPool.release(writeBuffer);
            }
        }
        bufferType = WRITE_MAP;
    }

    public long timestamp() {
        if (timestamp == -1L) {
            // 文件存在初始化时间戳
            readTimestamp();
        }
        return timestamp;
    }

    private void readTimestamp() {
        ByteBuffer timeBuffer = ByteBuffer.allocate(8);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
            fileChannel.position(0);
            fileChannel.read(timeBuffer);
        } catch (Exception e) {
            logger.error("Error to read timestamp from file: <{}> header, error: <{}>", file.getAbsolutePath(), e.getMessage());
        } finally {
            try {
                timestamp = timeBuffer.getLong(0);
            } catch (IndexOutOfBoundsException iobe) {
                logger.error("Error to read timestamp long value from file: <{}> header, error: <{}>", file.getAbsolutePath(), iobe.getMessage());
            }
        }
    }

    private void writeTimestamp() {
        ByteBuffer timeBuffer = ByteBuffer.allocate(8);
        long creationTime = System.currentTimeMillis();
        timeBuffer.putLong(0, creationTime);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
            fileChannel.position(0);
            fileChannel.write(timeBuffer);
            fileChannel.force(true);
        } catch (Exception e) {
            logger.error("Error to write timestamp from file: <{}> header, error: <{}>", file.getAbsolutePath(), e.getMessage());
        } finally {
            timestamp = creationTime;
        }
    }

    @Override
    public boolean unload() {
        long stamp = bufferLock.writeLock();
        try {
            if(isClean()) {
                unloadUnsafe();
                return true;
            } else {
                return false;
            }
        }finally {
            bufferLock.unlockWrite(stamp);
        }
    }

    @Override
    public boolean hasPage() {
        return this.bufferType != NO_BUFFER;
    }

    @Override
    public T read(int position, int length) throws IOException{
        if(this.bufferType == WRITE_MAP) {
            return readFromWriteMap(position);
        } else {
            return read(position, length, serializer);
        }

    }

    private T readFromWriteMap(int position) {
        touch();
        long stamp = bufferLock.readLock();
        try {
            T t = writeMap.get(position);
            if(null == t) throw new ReadException();
            return  t;
        } finally {
            bufferLock.unlockRead(stamp);
        }
    }

    public <R> R read(int position, int length, BufferReader<R> bufferReader) throws IOException{
        touch();
        long stamp = bufferLock.readLock();
        try {
            while (!hasPage()) {
                long ws = bufferLock.tryConvertToWriteLock(stamp);
                if(ws != 0L) {
                    // 升级成写锁成功
                    stamp = ws;
                    loadRoUnsafe();
                } else {
                    bufferLock.unlockRead(stamp);
                    stamp = bufferLock.writeLock();
                }
            }
            long rs = bufferLock.tryConvertToReadLock(stamp);
            if(rs != 0L) {
                stamp = rs;
            }
            ByteBuffer byteBuffer = pageBuffer.asReadOnlyBuffer();
            byteBuffer.position(position);
            byteBuffer.limit(writePosition);
            return bufferReader.read(byteBuffer, length);
        } finally {
            bufferLock.unlock(stamp);
        }
    }

    @Override
    public ByteBuffer readByteBuffer(int position, int length) throws IOException{
        if(this.bufferType == WRITE_MAP) {
            return readByteBufferFromWriteMap(position, length);
        } else {

            return read(position, Math.min(length, writePosition - position), (src, len) -> {
                ByteBuffer dest = ByteBuffer.allocate(len);
                if (len < src.remaining()) {
                    src.limit(src.position() + len);
                }
                dest.put(src);
                dest.flip();
                return dest;
            });
        }
    }

    private ByteBuffer readByteBufferFromWriteMap(int position, int length) {
        touch();
        long stamp = bufferLock.readLock();
        ByteBuffer byteBuffer = bufferPool.allocate(length);
        try {
            int pos = position;
            T t = writeMap.get(pos);
            while (byteBuffer.hasRemaining() && pos < writePosition) {
                if (t == null)
                    throw new ReadException();
                if (serializer.size(t) > byteBuffer.remaining()) {
                    break;
                }
                serializer.append(t, byteBuffer);
                pos += serializer.size(t);
                t = writeMap.get(pos);
            }
            byteBuffer.flip();
            return byteBuffer;
        } finally {
            bufferLock.unlockRead(stamp);
            bufferPool.release(byteBuffer);
        }
    }

    @Override
    public int append(T t) throws IOException{
        touch();
        long stamp = bufferLock.readLock();
        try {
            while (bufferType != DIRECT_BUFFER && bufferType != WRITE_MAP) {
                long ws = bufferLock.tryConvertToWriteLock(stamp);
                if(ws != 0L) {
                    // 升级成写锁成功
                    stamp = ws;
                    loadRwUnsafe();
                } else {
                    bufferLock.unlockRead(stamp);
                    stamp = bufferLock.writeLock();
                }
            }
            long rs = bufferLock.tryConvertToReadLock(stamp);
            if(rs != 0L) {
                stamp = rs;
            }
            if(this.bufferType == WRITE_MAP) {
                return appendToWriteMap(t);
            } else {
                return appendToPageBuffer(t, serializer);
            }
        } finally {
            bufferLock.unlock(stamp);
        }
    }

    private int appendToWriteMap(T t) {
        this.writeMap.put(writePosition, t);
        int writeLength = serializer.size(t);
        writePosition += writeLength;
        return writeLength;
    }

    // Not thread safe!
    private  <R> int appendToPageBuffer(R t, BufferAppender<R> bufferAppender) {
        pageBuffer.position(writePosition);
        int writeLength = bufferAppender.append(t, pageBuffer);
        writePosition += writeLength;
        return writeLength;
    }



    @Override
    public int appendByteBuffer(ByteBuffer byteBuffer) throws IOException{
        touch();
        long stamp = bufferLock.readLock();
        try {
            while (bufferType != DIRECT_BUFFER && bufferType != WRITE_MAP) {
                long ws = bufferLock.tryConvertToWriteLock(stamp);
                if(ws != 0L) {
                    // 升级成写锁成功
                    stamp = ws;
                    loadRwUnsafe();
                } else {
                    bufferLock.unlockRead(stamp);
                    stamp = bufferLock.writeLock();
                }
            }
            long rs = bufferLock.tryConvertToReadLock(stamp);
            if(rs != 0L) {
                stamp = rs;
            }
            if(this.bufferType == WRITE_MAP) {
                return appendByteBufferToWriteMap(byteBuffer);
            } else {
                return appendToPageBuffer(byteBuffer, (src, dest) -> {
                    int writeLength = src.remaining();
                    dest.put(src);
                    return writeLength;
                });
            }
        } finally {
            bufferLock.unlock(stamp);
        }
    }

    private int appendByteBufferToWriteMap(ByteBuffer byteBuffer) {
        int writeLength = byteBuffer.remaining();
        while (byteBuffer.hasRemaining()) {
            T t = serializer.read(byteBuffer, -1);
            writeMap.put(writePosition, t);
            writePosition += serializer.size(t);
        }
        return writeLength;
    }

    private void touch() {
        lastAccessTime = System.currentTimeMillis();
    }

    private AtomicBoolean flushGate = new AtomicBoolean(false);

    /**
     * 刷盘
     */
    // Not thread safe!
    @Override
    public int flush() throws IOException {
        long stamp = bufferLock.readLock();
        try {
            if (writePosition > flushPosition) {
                if (flushGate.compareAndSet(false, true)) {
                    if (!file.exists()) {
                        // 第一次创建文件写入头部预留128字节中0位置开始的前8字节长度:文件创建时间戳
                        writeTimestamp();
                    }
                    try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
                        return bufferType == WRITE_MAP ? flushWriteMap(fileChannel) : flushPageBuffer(fileChannel);
                    } finally {
                        flushGate.compareAndSet(true, false);
                    }
                } else {
                    throw new ConcurrentModificationException();
                }
            }
            return 0;
        } finally {
            bufferLock.unlockRead(stamp);
        }
    }

    private int flushWriteMap(FileChannel fileChannel)  throws IOException{
        int flushEnd = writePosition;
        int flushSize = 0;
        fileChannel.position(headerSize + flushPosition);
        ByteBuffer writeBuffer = bufferPool.allocate(Math.min(bufferLength, flushEnd - flushPosition));
        try {
            int pos = flushPosition;
            while (pos < flushEnd) {
                T t = writeMap.get(pos);
                int size = serializer.size(t);
                pos += size;
                if (size > writeBuffer.remaining()) {
                    writeBuffer.flip();
                    int length = writeBuffer.remaining();
                    fileChannel.write(writeBuffer);
                    flushSize += length;
                    writeBuffer.clear();
                }
                serializer.append(t, writeBuffer);
            }
            writeBuffer.flip();
            if (writeBuffer.hasRemaining()) {
                int length = writeBuffer.remaining();
                fileChannel.write(writeBuffer);
                flushSize += length;
                writeBuffer.clear();
            }
            flushPosition += flushSize;
            return flushSize;
        } finally {
            bufferPool.release(writeBuffer);
        }

    }

    private int flushPageBuffer(FileChannel fileChannel) throws IOException {
        int flushEnd = writePosition;
        ByteBuffer flushBuffer = pageBuffer.asReadOnlyBuffer();
        flushBuffer.position(flushPosition);
        flushBuffer.limit(flushEnd);
        fileChannel.position(headerSize + flushPosition);
        int flushSize = flushEnd - flushPosition;

        while (flushBuffer.hasRemaining()) {
            fileChannel.write(flushBuffer);
        }
        flushPosition = flushEnd;
        return flushSize;
    }

    // Not thread safe!
    @Override
    public void rollback(int position) throws IOException {
        if(position < writePosition) {
            if(WRITE_MAP == bufferType) {
                if(writeMap.containsKey(position)) {
                    int pos =  position;
                    while (pos < writePosition){
                        T t = writeMap.remove(pos);
                        pos += serializer.size(t);
                    }
                } else {
                    throw new RollBackException("Invalid rollback position: " + position);
                }
            }

            writePosition = position;
        }
        if (position < flushPosition) {
            if(flushGate.compareAndSet(false, true)) {
                try {
                    flushPosition = position;
                    try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
                        fileChannel.truncate(position + headerSize);
                    }
                }finally {
                    flushGate.compareAndSet(true, false);
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public boolean isClean() {
        return flushPosition >= writePosition;
    }

    @Override
    public int writePosition() {
        return writePosition;
    }

    @Override
    public int fileDataSize() {
        return Math.max((int)file.length() - headerSize, 0);
    }

    @Override
    public int flushPosition() {
        return flushPosition;
    }

    @Override
    public long lastAccessTime() {
        return lastAccessTime;
    }


    private void unloadUnsafe() {
        if (MAPPED_BUFFER == this.bufferType) {
            unloadMappedBuffer();
        } else if (DIRECT_BUFFER == this.bufferType) {
            unloadDirectBuffer();
        } else if (WRITE_MAP == this.bufferType) {
            unloadWriteMap();
        }
    }

    private void unloadDirectBuffer() {
        final ByteBuffer direct = pageBuffer;
        pageBuffer = null;
        this.bufferType = NO_BUFFER;
        if(null != direct) bufferPool.release(direct);
    }

    private void unloadMappedBuffer() {
        try {
            final Buffer mapped = pageBuffer;
            pageBuffer = null;
            this.bufferType = NO_BUFFER;
            if(null != mapped) {
                Method getCleanerMethod;
                getCleanerMethod = mapped.getClass().getMethod("cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(mapped, new Object[0]);
                cleaner.clean();
            }
        }catch (Exception e) {
            logger.warn("Release direct buffer exception: ", e);
        }
    }

    private void unloadWriteMap() {
        pageBuffer = null;
        this.bufferType = NO_BUFFER;
        this.writeMap.clear();
    }

}
