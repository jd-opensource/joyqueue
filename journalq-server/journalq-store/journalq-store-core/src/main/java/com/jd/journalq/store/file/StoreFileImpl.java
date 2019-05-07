/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.store.file;

import com.jd.journalq.store.utils.BufferHolder;
import com.jd.journalq.store.utils.PreloadBufferPool;
import com.jd.journalq.toolkit.time.SystemClock;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

/**
 * 支持并发、带缓存页、顺序写入的文件
 */
public class StoreFileImpl<T> implements StoreFile<T>, BufferHolder {
    private static final Logger logger = LoggerFactory.getLogger(StoreFileImpl.class);
    // 缓存页类型
    // 只读：
    // MAPPED_BUFFER：mmap映射内存镜像文件；
    // 读写：
    // DIRECT_BUFFER: 数据先写入DirectBuffer，异步刷盘到文件，性能最好；
    private static final int MAPPED_BUFFER = 0, DIRECT_BUFFER = 1, NO_BUFFER = -1;
    // 文件全局位置
    private final long filePosition;
    // 文件头长度
    private final int headerSize;
    // 对应的File
    private final File file;
    // buffer读写锁：
    // 访问(包括读和写）buffer时加读锁；
    // 加载、释放buffer时加写锁；
    private final StampedLock bufferLock = new StampedLock();
    private final LogSerializer<T> serializer;
    private final long createTimestamp;
    // 缓存页
    private ByteBuffer pageBuffer = null;
    private int bufferType = NO_BUFFER;
    private PreloadBufferPool bufferPool;
    private int capacity;
    private long lastAccessTime = SystemClock.now();
    // 当前刷盘位置
    private int flushPosition;
    // 当前写入位置
    private int writePosition = 0;
    private long timestamp = -1L;
    private AtomicBoolean flushGate = new AtomicBoolean(false);


    public StoreFileImpl(long filePosition, File base, int headerSize, LogSerializer<T> serializer, PreloadBufferPool bufferPool, int maxFileDataLength) {
        this.filePosition = filePosition;
        this.headerSize = headerSize;
        this.serializer = serializer;
        this.bufferPool = bufferPool;
        this.capacity = maxFileDataLength;
        this.file = new File(base, String.valueOf(filePosition));
        if (file.exists() && file.length() > headerSize) {
            this.writePosition = (int) (file.length() - headerSize);
            this.flushPosition = writePosition;
        }
        createTimestamp = SystemClock.now();

    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public long position() {
        return filePosition;
    }

    private void loadRoUnsafe() throws IOException {
        if (null != pageBuffer) throw new IOException("Buffer already loaded!");
        ByteBuffer loadBuffer;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
            loadBuffer =
                    fileChannel.map(FileChannel.MapMode.READ_ONLY, headerSize, file.length() - headerSize);
        }
        pageBuffer = loadBuffer;
        bufferType = MAPPED_BUFFER;
        pageBuffer.clear();
        bufferPool.addMemoryMappedBufferHolder(this);
    }

    private void loadRwUnsafe() throws IOException {
        if (bufferType == DIRECT_BUFFER) {
            return;
        } else if (bufferType == MAPPED_BUFFER) {
            unloadUnsafe();
        }
        ByteBuffer buffer = bufferPool.allocate(capacity, this);
        loadDirectBuffer(buffer);
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

    public long timestamp() {

        if (timestamp <= 0) {
            // 文件存在初始化时间戳
            timestamp = readTimestamp();
        }
        return timestamp;
    }

    private long readTimestamp() {
        if (file.exists() && file.length() > 8) {
            ByteBuffer timeBuffer = ByteBuffer.allocate(8);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r"); FileChannel fileChannel = raf.getChannel()) {
                fileChannel.position(0);
                fileChannel.read(timeBuffer);
                timeBuffer.flip();
                return timeBuffer.getLong();
            } catch (Exception e) {
                logger.warn("Error to read timestamp from file: {} header.", file.getAbsolutePath(), e);
            }
        }
        return createTimestamp;
    }

    private void writeTimestamp(FileChannel fileChannel) throws IOException {
        ByteBuffer timeBuffer = ByteBuffer.allocate(Long.BYTES);
        timestamp = createTimestamp;
        timeBuffer.putLong(timestamp);
        timeBuffer.flip();
        fileChannel.write(timeBuffer, 0L);
    }

    @Override
    public boolean unload() {
        long stamp = bufferLock.writeLock();
        try {
            if (isClean()) {
                unloadUnsafe();
                return true;
            } else {
                return false;
            }
        } finally {
            bufferLock.unlockWrite(stamp);
        }
    }

    @Override
    public void forceUnload() {
        long stamp = bufferLock.writeLock();
        try {
            unloadUnsafe();
        } finally {
            bufferLock.unlockWrite(stamp);
        }
    }

    @Override
    public boolean hasPage() {
        return this.bufferType != NO_BUFFER;
    }

    @Override
    public T read(int position, int length) throws IOException {
        return read(position, length, serializer);
    }

    public <R> R read(int position, int length, BufferReader<R> bufferReader) throws IOException {
        touch();
        long stamp = bufferLock.readLock();
        try {
            while (!hasPage()) {
                long ws = bufferLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    // 升级成写锁成功
                    stamp = ws;
                    loadRoUnsafe();
                } else {
                    bufferLock.unlockRead(stamp);
                    stamp = bufferLock.writeLock();
                }
            }
            long rs = bufferLock.tryConvertToReadLock(stamp);
            if (rs != 0L) {
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
    public ByteBuffer readByteBuffer(int position, int length) throws IOException {
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

    @Override
    public int append(T t) throws IOException {
        touch();
        long stamp = bufferLock.readLock();
        try {
            while (bufferType != DIRECT_BUFFER) {
                long ws = bufferLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    // 升级成写锁成功
                    stamp = ws;
                    loadRwUnsafe();
                } else {
                    bufferLock.unlockRead(stamp);
                    stamp = bufferLock.writeLock();
                }
            }
            long rs = bufferLock.tryConvertToReadLock(stamp);
            if (rs != 0L) {
                stamp = rs;
            }
            return appendToPageBuffer(t, serializer);
        } finally {
            bufferLock.unlock(stamp);
        }
    }

    // Not thread safe!
    private <R> int appendToPageBuffer(R t, BufferAppender<R> bufferAppender) {
        pageBuffer.position(writePosition);
        int writeLength = bufferAppender.append(t, pageBuffer);
        writePosition += writeLength;
        return writeLength;
    }

    @Override
    public int appendByteBuffer(ByteBuffer byteBuffer) throws IOException {
        touch();
        long stamp = bufferLock.readLock();
        try {
            while (bufferType != DIRECT_BUFFER) {
                long ws = bufferLock.tryConvertToWriteLock(stamp);
                if (ws != 0L) {
                    // 升级成写锁成功
                    stamp = ws;
                    loadRwUnsafe();
                } else {
                    bufferLock.unlockRead(stamp);
                    stamp = bufferLock.writeLock();
                }
            }
            long rs = bufferLock.tryConvertToReadLock(stamp);
            if (rs != 0L) {
                stamp = rs;
            }
            return appendToPageBuffer(byteBuffer, (src, dest) -> {
                int writeLength = src.remaining();
                dest.put(src);
                return writeLength;
            });
        } finally {
            bufferLock.unlock(stamp);
        }
    }

    private void touch() {
        lastAccessTime = SystemClock.now();
    }

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
                    try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
                        if (flushPosition == 0) {
                            writeTimestamp(fileChannel);
                        }
                        return flushPageBuffer(fileChannel);
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
        if (position < writePosition) {
            writePosition = position;
        }
        if (position < flushPosition) {
            if (flushGate.compareAndSet(false, true)) {
                try {
                    flushPosition = position;
                    try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fileChannel = raf.getChannel()) {
                        fileChannel.truncate(position + headerSize);
                    }
                } finally {
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
        return Math.max((int) file.length() - headerSize, 0);
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
        }
    }

    private void unloadDirectBuffer() {
        final ByteBuffer direct = pageBuffer;
        pageBuffer = null;
        this.bufferType = NO_BUFFER;
        if (null != direct) bufferPool.release(direct, this);
    }

    private void unloadMappedBuffer() {
        try {
            final Buffer mapped = pageBuffer;
            pageBuffer = null;
            this.bufferType = NO_BUFFER;
            if (null != mapped) {
                Method getCleanerMethod;
                getCleanerMethod = mapped.getClass().getMethod("cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(mapped, new Object[0]);
                cleaner.clean();
            }
        } catch (Exception e) {
            logger.warn("Release direct buffer exception: ", e);
        }
    }


    @Override
    public int size() {
        return capacity;
    }

    @Override
    public boolean isFree() {
        return isClean();
    }

    @Override
    public boolean evict() {
        return unload();
    }
}
