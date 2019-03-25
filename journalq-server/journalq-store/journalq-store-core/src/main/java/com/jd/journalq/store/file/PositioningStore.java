package com.jd.journalq.store.file;

import com.jd.journalq.store.PositionOverflowException;
import com.jd.journalq.store.PositionUnderflowException;
import com.jd.journalq.store.utils.PreloadBufferPool;
import com.jd.journalq.store.utils.ThreadSafeFormat;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * 带缓存的、无锁、高性能、多文件、基于位置的、Append Only的日志存储存储。
 * @author liyue25
 * Date: 2018/8/14
 */
public class PositioningStore<T> implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(PositioningStore.class);
    private final int fileHeaderSize;
    private final int fileDataSize;
    private final int maxPageCount;
    private final long cacheLifeTime;
    private final int bufferLength;
    private final File base;
    private final LogSerializer<T> serializer;
    private final PreloadBufferPool bufferPool;
    private final NavigableMap<Long, StoreFile<T>> storeFileMap = new ConcurrentSkipListMap<>();
    private long flushPosition = 0L;
    private long writePosition = 0L;
    private long leftPosition = 0L;
    // 正在写入的
    private StoreFile<T> writeStoreFile = null;

    public PositioningStore(File base, Config config, PreloadBufferPool bufferPool, LogSerializer<T> serializer) {
        this.base = base;
        this.fileHeaderSize = config.fileHeaderSize;
        this.fileDataSize = config.fileDataSize;
        this.maxPageCount = config.cachedPageCount;
        this.cacheLifeTime = config.cacheLifeTime;
        this.bufferLength = config.bufferLength;
        this.bufferPool = bufferPool;
        this.serializer = serializer;
    }

    public long left() {
        return leftPosition;
    }

    public long right() {
        return writePosition;
    }
    public long flushPosition() {
        return flushPosition ;
    }

    /**
     * 将位置回滚到position
     * 与如下操作不能并发：
     * flush()
     * append()
     */
    public void setRight(long position) throws IOException {

        if(position == right()) return;
        logger.info("Rollback to position: {}, left: {}, right: {}, flushPosition: {}, store: {}...",
                 ThreadSafeFormat.formatWithComma(position),
                ThreadSafeFormat.formatWithComma(leftPosition),
                ThreadSafeFormat.formatWithComma(writePosition),
                ThreadSafeFormat.formatWithComma(flushPosition()),
                base.getAbsolutePath());

        if (position <= leftPosition || position > right()) {
            clear();
            this.leftPosition = position;
            this.writePosition = position;
            this.flushPosition = position;
        } else if (position < right()) {
            rollbackFiles(position);
            this.writePosition = position;
            if(this.flushPosition > position) this.flushPosition = position;
            resetWriteStoreFile();
        }
    }

    public void clear() {
        for(StoreFile<T> storeFile :this.storeFileMap.values()) {
            if(storeFile.hasPage()) storeFile.unload();
            File file = storeFile.file();
            if(file.exists() && !file.delete())
                throw new RollBackException(String.format("Can not delete file: %s.", file.getAbsolutePath()));
        }
        this.storeFileMap.clear();
    }

    private void rollbackFiles(long position) throws IOException {

        if(!storeFileMap.isEmpty()) {
            // position 所在的Page需要截断至position
            Map.Entry<Long, StoreFile<T>> entry = storeFileMap.floorEntry(position);
            StoreFile storeFile = entry.getValue();
            if(position > storeFile.position()) {
                int relPos = (int) (position - storeFile.position());
                logger.info("Truncate store file {} to relative position {}.", storeFile.file().getAbsolutePath(), relPos);
                storeFile.rollback(relPos);
            }

            SortedMap<Long, StoreFile<T>> toBeRemoved = storeFileMap.tailMap(position);

            for(StoreFile sf : toBeRemoved.values()) {
                logger.info("Delete store file {}.", sf.file().getAbsolutePath());
                deleteStoreFile(sf);
            }
            toBeRemoved.clear();
        }


    }


    private void resetWriteStoreFile() {
        if(!storeFileMap.isEmpty()) {
            StoreFile<T> storeFile = storeFileMap.lastEntry().getValue();
            if(storeFile.position() + fileDataSize > writePosition) {
                writeStoreFile = storeFile;
            }
        }
    }

    public void recover() throws IOException {
        recoverFileMap();

        long recoverPosition = this.storeFileMap.isEmpty()? 0L : this.storeFileMap.lastKey() + this.storeFileMap.lastEntry().getValue().fileDataSize();
        flushPosition = recoverPosition;
        writePosition = recoverPosition;
        leftPosition = this.storeFileMap.isEmpty()? 0L : this.storeFileMap.firstKey();

        if(recoverPosition > 0) {
            long lastLogTail = toLogTail(recoverPosition - 1);
            if(lastLogTail < 0) {
                throw new CorruptedLogException(String.format("Unable to read any valid log. Corrupted log files: %s.", base.getAbsolutePath()));
            }
            if (lastLogTail < recoverPosition) {
                rollbackFiles(lastLogTail);
                flushPosition = lastLogTail;
                writePosition = lastLogTail;

            }
        }
        resetWriteStoreFile();
        logger.info("Store loaded, left: {}, right: {},  base: {}.",
                ThreadSafeFormat.formatWithComma(left()),
                ThreadSafeFormat.formatWithComma(right()),
                base.getAbsolutePath());
    }

    private void recoverFileMap() {
        File[] files = base.listFiles(file -> file.isFile() && file.getName().matches("\\d+"));
        long filePosition;
        if(null != files) {
            for (File file : files) {
                filePosition = Long.parseLong(file.getName());
                storeFileMap.put(filePosition, new StoreFileImpl<>(filePosition, base, fileHeaderSize, serializer, bufferPool, fileDataSize, bufferLength));
//                storeFileMap.put(filePosition, new FastWriteStoreFile<>(filePosition, base, fileHeaderSize, serializer, 10 * 1024 * 1024));
            }
        }

        // 检查文件是否连续完整
        if(!storeFileMap.isEmpty()) {
            long position = storeFileMap.firstKey();
            for (Map.Entry<Long, StoreFile<T>> fileEntry : storeFileMap.entrySet()) {
                if(position != fileEntry.getKey()) {
                    throw new CorruptedLogException(String.format("Files are not continuous! expect: %d, actual file name: %d, store: %s.", position, fileEntry.getKey(), base.getAbsolutePath()));
                }
                position += fileEntry.getValue().file().length() - fileHeaderSize;
            }
        }
    }

    private long toLogTail(long position) {
        T t = null;
        while (position >= left()) {
            try{
                t = tryRead(position--);
            }catch (Throwable ignored) {}
            if(null != t) return position + 1 + serializer.size(t);
        }
        return  -1L;
    }

    public long toLogStart(long position) {

        T t = null;
        while (position >= left()) {
            try{
                t = tryRead(position--);
            }catch (Throwable ignored) {}
            if(null != t) return position + 1;
        }
        return  -1L;
    }

    /**
     * 写入一条日志
     * @param byteBuffer 存放日志的ByteBuffer
     * @return 写入结束位置
     */
    public long appendByteBuffer(ByteBuffer byteBuffer) throws IOException{
        if (null == writeStoreFile) writeStoreFile = createStoreFile(writePosition);
        if (fileDataSize - writeStoreFile.writePosition() < byteBuffer.remaining()) writeStoreFile = createStoreFile(writePosition);

        writePosition += writeStoreFile.appendByteBuffer(byteBuffer);

        return writePosition;
    }

    public long append(T t) throws IOException{
        if (null == writeStoreFile) writeStoreFile = createStoreFile(writePosition);
        if (fileDataSize - writeStoreFile.writePosition() < serializer.size(t)) writeStoreFile = createStoreFile(writePosition);
        writePosition += writeStoreFile.append(t);
        return writePosition;
    }

    public long append(final List<T> ts) throws IOException{
        if(null == ts || ts.isEmpty()) throw new WriteException("Parameter list is empty!");
        long position = 0;
        for (T t: ts) {
            position = append(t);
        }
        return position;
    }

    public boolean flush() throws IOException {
        boolean ret = false;
        while (flushPosition < writePosition) {
            Map.Entry<Long, StoreFile<T>> entry = storeFileMap.floorEntry(flushPosition);
            if (null == entry) return false;
            StoreFile storeFile = entry.getValue();
            if(!storeFile.isClean()) storeFile.flush();
            if(flushPosition < storeFile.position() + storeFile.flushPosition()) {
                flushPosition = storeFile.position() + storeFile.flushPosition();
            } else {
                // 永远不会走到这里，除非程序bug了。
                throw new CorruptedLogException(String.format("ZERO length flushed! " +
                                "File: position: %d, writePosition: %d, flushPosition: %d. " +
                        "Store: writePosition: %d, flushPosition: %d, leftPosition: %d, store: %s.",
                        storeFile.position(), storeFile.writePosition(), storeFile.flushPosition(),
                        writePosition, flushPosition, leftPosition, base.getAbsolutePath()));
            }
            ret = true;
        }


//        if(evict()) ret = true;
        return ret;
    }


    private StoreFile<T> createStoreFile(long position) {
        StoreFile<T> storeFile = new StoreFileImpl<>(position, base, fileHeaderSize, serializer, bufferPool, fileDataSize, bufferLength);
        StoreFile<T> present;
        if((present = storeFileMap.putIfAbsent(position, storeFile)) != null){
            storeFile = present;
        }

        return storeFile;
    }

    private static class LruWrapper<V> {
        private final long lastAccessTime;
        private final V t;
        LruWrapper(V t, long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
            this.t = t;
        }
        private long getLastAccessTime() {
            return lastAccessTime;
        }

        private V get() {
            return t;
        }
    }

    /**
     * 清除文件缓存页。LRU。
     */
    public boolean evict() {
        boolean ret = false;
        if(storeFileMap.isEmpty()) return ret;
        List<LruWrapper<StoreFile>> sorted;
        sorted = storeFileMap.values().stream()
                .filter(StoreFile::hasPage)
                .map(storeFile -> new LruWrapper<StoreFile>(storeFile, storeFile.lastAccessTime()))
                .sorted(Comparator.comparing(LruWrapper::getLastAccessTime))
                .collect(Collectors.toList());

        long now = SystemClock.now();
        int count = sorted.size();
        while (!sorted.isEmpty()) {
            LruWrapper<StoreFile> storeFileWrapper = sorted.remove(0);
            StoreFile storeFile = storeFileWrapper.get();
            if(storeFile.lastAccessTime() == storeFileWrapper.getLastAccessTime()
                    && (count > maxPageCount // 已经超过缓存数量限制
                        || storeFileWrapper.getLastAccessTime() + cacheLifeTime > now)){ // 或者缓存太久没有被访问
                if(storeFile.unload()) {
                    count--;
                    ret = true;
                }
            }
        }
        return ret;
    }
    public T read(long position) throws IOException{
        checkReadPosition(position);
        try {
            return tryRead(position);
        } catch (Throwable t) {
            logger.warn("Exception on read position {} of store {}.", position, base.getAbsolutePath(), t);
            throw t;
        }
    }

    public T read(long position, int length) throws IOException{
        checkReadPosition(position);
        try {
            StoreFile<T> storeFile = storeFileMap.floorEntry(position).getValue();
            int relPosition = (int )(position - storeFile.position());
            return storeFile.read(relPosition, length);
        } catch (Throwable t) {
            logger.warn("Exception on read position {} of store {}.", position, base.getAbsolutePath(), t);
            throw t;
        }
    }

    private T tryRead(long position) throws IOException{

        checkReadPosition(position);
        StoreFile<T> storeFile = storeFileMap.floorEntry(position).getValue();
        int relPosition = (int )(position - storeFile.position());
        return storeFile.read(relPosition, -1);
    }

    public List<T> batchRead(long position, int count) throws IOException{
        checkReadPosition(position);
        List<T> list = new ArrayList<>(count);
        long pointer = position;

        StoreFile<T> storeFile = null;
        try {
            while (list.size() < count && pointer < writePosition) {

                if (null == storeFile || storeFile.writePosition() + storeFile.position() <= pointer) {
                    storeFile = storeFileMap.floorEntry(pointer).getValue();
                }

                int relPosition = (int) (pointer - storeFile.position());
                T t = storeFile.read(relPosition, -1);
                list.add(t);
                pointer += serializer.size(t);

            }

            return list;
        } catch (Throwable t) {
            logger.warn("Exception on batchRead position {} of store {}.", pointer, base.getAbsolutePath(), t);
            throw t;
        }

    }

    public ByteBuffer readByteBuffer(long position, int length) throws IOException{
        checkReadPosition(position);
        try {
            StoreFile storeFile = storeFileMap.floorEntry(position).getValue();
            int relPosition = (int )(position - storeFile.position());
            ByteBuffer byteBuffer = storeFile.readByteBuffer(relPosition, length);
            byteBuffer.limit(byteBuffer.position() +  serializer.trim(byteBuffer, length));
            return byteBuffer;
        } catch (Throwable t) {
            logger.warn("Exception on read position {} of store {}.", position, base.getAbsolutePath(), t);
            throw t;
        }
    }

    /**
     * 获取文件实际数据长度，不包含文件头
     */
    private long getDataSize(File file){
        long size = file.length();
        return size > fileHeaderSize ? size - fileHeaderSize: 0L;
    }


    private void checkReadPosition(long position){
        long p;
        if((p = leftPosition) > position) {
            throw new PositionUnderflowException(position, p);
        } else if(position >= (p = writePosition)) {
            throw new PositionOverflowException(position, p);
        }

    }

    public long physicalSize() {
        return storeFileMap.values().stream().map(StoreFile::file).mapToLong(File::length).sum();
    }

    /**
     * 删除 position之前的文件
     */
    public long physicalDeleteTo(long position) throws IOException {

        if(position > flushPosition) position = flushPosition;

        Iterator<Map.Entry<Long, StoreFile<T>>> iterator =
                storeFileMap.entrySet().iterator();
        long deleteSize = 0L;

        while (iterator.hasNext()) {
            Map.Entry<Long, StoreFile<T>> entry = iterator.next();
            StoreFile<T> storeFile = entry.getValue();
            long start = entry.getKey();
            long fileDataSize = storeFile.hasPage()? storeFile.writePosition(): storeFile.fileDataSize();

            // 至少保留一个文件
            if(storeFileMap.size() < 2 || start + fileDataSize > position) break;
            leftPosition += fileDataSize;
            iterator.remove();

            deleteSize += deleteStoreFile(storeFile);
        }

        return deleteSize;

    }

    public boolean isClean() {
        return flushPosition == writePosition;
    }

    public long physicalDeleteLeftFile() throws IOException {
        if(storeFileMap.isEmpty()) return 0;
        StoreFile storeFile = storeFileMap.firstEntry().getValue();
        return physicalDeleteTo(storeFile.position() + (storeFile.hasPage() ? storeFile.writePosition(): storeFile.fileDataSize()));
    }

    private long deleteStoreFile(StoreFile storeFile) throws IOException {
        if(storeFile.isClean()) {
            storeFile.unload();
        }
        File file = storeFile.file();
        long fileSize = file.length();
        if(file.exists()) {
            if (file.delete()) {
                logger.debug("File {} deleted.", file.getAbsolutePath());
                return fileSize;
            } else {
                throw new IOException(String.format("Delete file %s failed!", file.getAbsolutePath()));
            }
        } else {
            return 0;
        }
    }

    /**
     * 获得日志的相对给定位置的位置（怎么这么绕？）
     * 先找到position所在的日志的起始位置，然后：
     * 向前(offsetCount为负值时)/向后(offsetCount为正值时
     * 偏移offsetCount条日志，返回日志的位置
     * 如果到了左右边界，直接返回左右边界位置。
     * @param position 给定的绝对位置
     * @param offsetCount 偏移日志条数，可以为负值
     */
    public long position(long position, int offsetCount) {

        int offset = 0;
        long pos = position;
        if(pos < left()) {
            pos = left();
        } else if(pos > right()){
            pos = right();
        } else if(left() < pos && pos < right()){
            pos = toLogStart(position);
        }

        if(offsetCount > 0) {
            while (offset < offsetCount && pos < right()) {
                pos = toLogTail(pos);
                offset ++;
            }
        } else if (offsetCount < 0) {
            while (offset > offsetCount && pos > left()) {
                pos = toLogStart(pos - 1);
                offset --;
            }

        }

        return pos;
    }


    public File base() {
        return base;
    }

    @Override
    public void close() {
        for(StoreFile storeFile : storeFileMap.values()) {
            storeFile.unload();
        }
    }

    public byte[] readBytes(long position, int length) throws IOException{
        checkReadPosition(position);
        StoreFile storeFile = storeFileMap.floorEntry(position).getValue();
        int relPosition = (int )(position - storeFile.position());
        return  storeFile.readByteBuffer(relPosition, length).array();
    }

    public int fileCount() {
        return storeFileMap.size();
    }

    public boolean meetMinStoreFile(long minIndexedPhysicalPosition) {
        return storeFileMap.headMap(minIndexedPhysicalPosition).size() > 0;
    }

    public boolean isEarly(long timestamp, long minIndexedPhysicalPosition) {
        for (StoreFile<T> storeFile : storeFileMap.headMap(minIndexedPhysicalPosition).values()) {
            if (storeFile.timestamp() < timestamp) {
                return true;
            }
        }
        return false;
    }

    public static class Config {
        public final static int DEFAULT_FILE_HEADER_SIZE = 128;
        public final static int DEFAULT_FILE_DATA_SIZE = 128 * 1024 * 1024;
        public final static int DEFAULT_CACHED_PAGE_COUNT = 2;
        public final static long DEFAULT_CACHE_LIFETIME_MS = 5000L;
        public final static int DEFAULT_BUFFER_LENGTH = 1024 * 1024;



        /**
         * 文件头长度
         */
        private final int fileHeaderSize;
        /**
         * 文件内数据最大长度
         */
        private final int fileDataSize;
        /**
         * 最多缓存的页面数量
         */
        private final int cachedPageCount;

        /**
         * 缓存最长存活时间
         */
        private final long cacheLifeTime;

        /**
         * 写文件的缓冲区长度
         */
        private final int bufferLength;


        public Config(){
            this(DEFAULT_FILE_DATA_SIZE, DEFAULT_CACHED_PAGE_COUNT,
                    DEFAULT_FILE_HEADER_SIZE,
                    DEFAULT_CACHE_LIFETIME_MS,
                    DEFAULT_BUFFER_LENGTH);
        }
        public Config(int fileDataSize){
            this(fileDataSize, DEFAULT_CACHED_PAGE_COUNT,
                    DEFAULT_FILE_HEADER_SIZE,
                    DEFAULT_CACHE_LIFETIME_MS,
                    DEFAULT_BUFFER_LENGTH);
        }

        public Config(int fileDataSize, int bufferLength){
            this(fileDataSize, DEFAULT_CACHED_PAGE_COUNT,
                    DEFAULT_FILE_HEADER_SIZE,
                    DEFAULT_CACHE_LIFETIME_MS,
                    bufferLength);
        }

        public Config(int fileDataSize, int cachedPageCount, int fileHeaderSize, long cacheLifeTime, int bufferLength){
            this.fileDataSize = fileDataSize;
            this.cachedPageCount = cachedPageCount;
            this.fileHeaderSize = fileHeaderSize;
            this.cacheLifeTime = cacheLifeTime;
            this.bufferLength = bufferLength;
        }
    }



}
