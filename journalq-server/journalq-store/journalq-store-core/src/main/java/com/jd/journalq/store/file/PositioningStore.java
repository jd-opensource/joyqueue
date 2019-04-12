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

import com.jd.journalq.store.PositionOverflowException;
import com.jd.journalq.store.PositionUnderflowException;
import com.jd.journalq.store.utils.PreloadBufferPool;
import com.jd.journalq.toolkit.format.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 带缓存的、无锁、高性能、多文件、基于位置的、Append Only的日志存储存储。
 *
 * @author liyue25
 * Date: 2018/8/14
 */
public class PositioningStore<T> implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(PositioningStore.class);
    private final int fileHeaderSize;
    private final int fileDataSize;
    private final File base;
    private final LogSerializer<T> serializer;
    private final PreloadBufferPool bufferPool;
    private final NavigableMap<Long, StoreFile<T>> storeFileMap = new ConcurrentSkipListMap<>();
    private final AtomicLong flushPosition = new AtomicLong(0L);
    private final AtomicLong writePosition = new AtomicLong(0L);
    private final AtomicLong leftPosition = new AtomicLong(0L);
    // 删除和回滚不能同时操作flushPosition，需要做一下互斥。
    private final Object flushPositionMutex = new Object();
    // 正在写入的
    private StoreFile<T> writeStoreFile = null;

    public PositioningStore(File base, Config config, PreloadBufferPool bufferPool, LogSerializer<T> serializer) {
        this.base = base;
        this.fileHeaderSize = config.fileHeaderSize;
        this.fileDataSize = config.fileDataSize;
        this.bufferPool = bufferPool;
        this.serializer = serializer;
    }

    public long left() {
        return leftPosition.get();
    }

    public long right() {
        return writePosition.get();
    }

    public long flushPosition() {
        return flushPosition.get();
    }

    /**
     * 将位置回滚到position
     * 与如下操作不能并发：
     * flush()
     * append()
     * physicalDeleteTo()
     */
    public void setRight(long position) throws IOException {

        synchronized (flushPositionMutex) {
            if (position == right()) return;
            logger.info("Rollback to position: {}, left: {}, right: {}, flushPosition: {}, store: {}...",
                    Format.formatWithComma(position),
                    Format.formatWithComma(leftPosition.get()),
                    Format.formatWithComma(writePosition.get()),
                    Format.formatWithComma(flushPosition()),
                    base.getAbsolutePath());

            if (position <= leftPosition.get() || position > right()) {
                clear();
                this.leftPosition.set(position);
                this.writePosition.set(position);
                this.flushPosition.set(position);
            } else if (position < right()) {
                rollbackFiles(position);
                this.writePosition.set(position);
                if (this.flushPosition.get() > position) this.flushPosition.set(position);
                resetWriteStoreFile();
            }
        }
    }

    public void clear() {
        for (StoreFile<T> storeFile : this.storeFileMap.values()) {
            if (storeFile.hasPage()) storeFile.unload();
            File file = storeFile.file();
            if (file.exists() && !file.delete())
                throw new RollBackException(String.format("Can not delete file: %s.", file.getAbsolutePath()));
        }
        this.storeFileMap.clear();
    }

    private void rollbackFiles(long position) throws IOException {

        if (!storeFileMap.isEmpty()) {
            // position 所在的Page需要截断至position
            Map.Entry<Long, StoreFile<T>> entry = storeFileMap.floorEntry(position);
            StoreFile storeFile = entry.getValue();
            if (position > storeFile.position()) {
                int relPos = (int) (position - storeFile.position());
                logger.info("Truncate store file {} to relative position {}.", storeFile.file().getAbsolutePath(), relPos);
                storeFile.rollback(relPos);
            }

            SortedMap<Long, StoreFile<T>> toBeRemoved = storeFileMap.tailMap(position);

            for (StoreFile sf : toBeRemoved.values()) {
                logger.info("Delete store file {}.", sf.file().getAbsolutePath());
                forceDeleteStoreFile(sf);
            }
            toBeRemoved.clear();
        }


    }


    private void resetWriteStoreFile() {
        if (!storeFileMap.isEmpty()) {
            StoreFile<T> storeFile = storeFileMap.lastEntry().getValue();
            if (storeFile.position() + fileDataSize > writePosition.get()) {
                writeStoreFile = storeFile;
            }
        }
    }

    public void recover() throws IOException {
        recoverFileMap();

        long recoverPosition = this.storeFileMap.isEmpty() ? 0L : this.storeFileMap.lastKey() + this.storeFileMap.lastEntry().getValue().fileDataSize();
        flushPosition.set(recoverPosition);
        writePosition.set(recoverPosition);
        leftPosition.set(this.storeFileMap.isEmpty() ? 0L : this.storeFileMap.firstKey());

        if (recoverPosition > 0) {
            long lastLogTail = toLogTail(recoverPosition - 1);
            if (lastLogTail < 0) {
                throw new CorruptedLogException(String.format("Unable to read any valid log. Corrupted log files: %s.", base.getAbsolutePath()));
            }
            if (lastLogTail < recoverPosition) {
                rollbackFiles(lastLogTail);
                flushPosition.set(lastLogTail);
                writePosition.set(lastLogTail);

            }
        }
        resetWriteStoreFile();
        logger.info("Store loaded, left: {}, right: {},  base: {}.",
                Format.formatWithComma(left()),
                Format.formatWithComma(right()),
                base.getAbsolutePath());
    }

    private void recoverFileMap() {
        File[] files = base.listFiles(file -> file.isFile() && file.getName().matches("\\d+"));
        long filePosition;
        if (null != files) {
            for (File file : files) {
                filePosition = Long.parseLong(file.getName());
                storeFileMap.put(filePosition, new StoreFileImpl<>(filePosition, base, fileHeaderSize, serializer, bufferPool, fileDataSize));
//                storeFileMap.put(filePosition, new FastWriteStoreFile<>(filePosition, base, fileHeaderSize, serializer, 10 * 1024 * 1024));
            }
        }

        // 检查文件是否连续完整
        if (!storeFileMap.isEmpty()) {
            long position = storeFileMap.firstKey();
            for (Map.Entry<Long, StoreFile<T>> fileEntry : storeFileMap.entrySet()) {
                if (position != fileEntry.getKey()) {
                    throw new CorruptedLogException(String.format("Files are not continuous! expect: %d, actual file name: %d, store: %s.", position, fileEntry.getKey(), base.getAbsolutePath()));
                }
                position += fileEntry.getValue().file().length() - fileHeaderSize;
            }
        }
    }

    private long toLogTail(long position) {
        T t = null;
        while (position >= left()) {
            try {
                t = tryRead(position--);
            } catch (Throwable ignored) {
            }
            if (null != t) return position + 1 + serializer.size(t);
        }
        return -1L;
    }

    public long toLogStart(long position) {

        T t = null;
        while (position >= left()) {
            try {
                t = tryRead(position--);
            } catch (Throwable ignored) {
            }
            if (null != t) return position + 1;
        }
        return -1L;
    }

    /**
     * 写入一条日志
     *
     * @param byteBuffer 存放日志的ByteBuffer
     * @return 写入结束位置
     */
    public long appendByteBuffer(ByteBuffer byteBuffer) throws IOException {
        if (null == writeStoreFile) writeStoreFile = createStoreFile(writePosition.get());
        if (fileDataSize - writeStoreFile.writePosition() < byteBuffer.remaining())
            writeStoreFile = createStoreFile(writePosition.get());

        writePosition.getAndAdd(writeStoreFile.appendByteBuffer(byteBuffer));

        return writePosition.get();
    }

    public long append(T t) throws IOException {
        if (null == writeStoreFile) writeStoreFile = createStoreFile(writePosition.get());
        if (fileDataSize - writeStoreFile.writePosition() < serializer.size(t))
            writeStoreFile = createStoreFile(writePosition.get());
        writePosition.getAndAdd(writeStoreFile.append(t));
        return writePosition.get();
    }

    public long append(final List<T> ts) throws IOException {
        if (null == ts || ts.isEmpty()) throw new WriteException("Parameter list is empty!");
        long position = 0;
        for (T t : ts) {
            position = append(t);
        }
        return position;
    }

    public boolean flush() throws IOException {
        if (flushPosition.get() < writePosition.get()) {
            synchronized (flushPositionMutex) {
                Map.Entry<Long, StoreFile<T>> entry = storeFileMap.floorEntry(flushPosition.get());
                if (null == entry) {
                    if (storeFileMap.isEmpty()) {
                        flushPosition.set(writePosition.get());
                    } else {
                        flushPosition.set(storeFileMap.firstKey());
                    }
                    return true;
                }
                StoreFile storeFile = entry.getValue();
                if (!storeFile.isClean()) storeFile.flush();
                if (flushPosition.get() < storeFile.position() + storeFile.flushPosition()) {
                    flushPosition.set(storeFile.position() + storeFile.flushPosition());
                } else {
                    // 永远不会走到这里，除非程序bug了。
                    throw new CorruptedLogException(String.format("ZERO length flushed! " +
                                    "File: position: %d, writePosition: %d, flushPosition: %d. " +
                                    "Store: writePosition: %d, flushPosition: %d, leftPosition: %d, store: %s.",
                            storeFile.position(), storeFile.writePosition(), storeFile.flushPosition(),
                            writePosition.get(), flushPosition.get(), leftPosition.get(), base.getAbsolutePath()));
                }
                return true;
            }
        }
        return false;
    }


    private StoreFile<T> createStoreFile(long position) {
        StoreFile<T> storeFile = new StoreFileImpl<>(position, base, fileHeaderSize, serializer, bufferPool, fileDataSize);
        StoreFile<T> present;
        if ((present = storeFileMap.putIfAbsent(position, storeFile)) != null) {
            storeFile = present;
        }

        return storeFile;
    }

    public T read(long position) throws IOException {
        checkReadPosition(position);
        try {
            return tryRead(position);
        } catch (Throwable t) {
            logger.warn("Exception on read position {} of store {}.", position, base.getAbsolutePath(), t);
            throw t;
        }
    }

    public T read(long position, int length) throws IOException {
        checkReadPosition(position);
        try {
            StoreFile<T> storeFile = storeFileMap.floorEntry(position).getValue();
            int relPosition = (int) (position - storeFile.position());
            return storeFile.read(relPosition, length);
        } catch (Throwable t) {
            logger.warn("Exception on read position {} of store {}.", position, base.getAbsolutePath(), t);
            throw t;
        }
    }

    private T tryRead(long position) throws IOException {

        checkReadPosition(position);
        StoreFile<T> storeFile = storeFileMap.floorEntry(position).getValue();
        int relPosition = (int) (position - storeFile.position());
        return storeFile.read(relPosition, -1);
    }

    public List<T> batchRead(long position, int count) throws IOException {
        checkReadPosition(position);
        List<T> list = new ArrayList<>(count);
        long pointer = position;

        StoreFile<T> storeFile = null;
        try {
            while (list.size() < count && pointer < writePosition.get()) {

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

    public ByteBuffer readByteBuffer(long position, int length) throws IOException {
        checkReadPosition(position);
        try {
            StoreFile storeFile = storeFileMap.floorEntry(position).getValue();
            int relPosition = (int) (position - storeFile.position());
            ByteBuffer byteBuffer = storeFile.readByteBuffer(relPosition, length);
            byteBuffer.limit(byteBuffer.position() + serializer.trim(byteBuffer, length));
            return byteBuffer;
        } catch (Throwable t) {
            logger.warn("Exception on read position {} of store {}.", position, base.getAbsolutePath(), t);
            throw t;
        }
    }


    private void checkReadPosition(long position) {
        long p;
        if ((p = leftPosition.get()) > position) {
            throw new PositionUnderflowException(position, p);
        } else if (position >= (p = writePosition.get())) {
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

        synchronized (flushPositionMutex) {
            if (position > flushPosition.get()) position = flushPosition.get();

            Iterator<Map.Entry<Long, StoreFile<T>>> iterator =
                    storeFileMap.entrySet().iterator();
            long deleteSize = 0L;

            while (iterator.hasNext()) {
                Map.Entry<Long, StoreFile<T>> entry = iterator.next();
                StoreFile<T> storeFile = entry.getValue();
                long start = entry.getKey();
                long fileDataSize = storeFile.hasPage() ? storeFile.writePosition() : storeFile.fileDataSize();

                // 至少保留一个文件
                if (storeFileMap.size() < 2 || start + fileDataSize > position) break;
                leftPosition.getAndAdd(fileDataSize);
                if (flushPosition.get() < leftPosition.get()) flushPosition.set(leftPosition.get());
                iterator.remove();
                forceDeleteStoreFile(storeFile);
                deleteSize += fileDataSize;
            }

            return deleteSize;

        }
    }

    public boolean isClean() {
        return flushPosition.get() == writePosition.get();
    }

    public long physicalDeleteLeftFile() throws IOException {
        if (storeFileMap.isEmpty()) return 0;
        StoreFile storeFile = storeFileMap.firstEntry().getValue();
        return physicalDeleteTo(storeFile.position() + (storeFile.hasPage() ? storeFile.writePosition() : storeFile.fileDataSize()));
    }

    /**
     * 删除文件，丢弃未刷盘的数据，用于rollback
     */
    private void forceDeleteStoreFile(StoreFile storeFile) throws IOException {
        storeFile.forceUnload();
        File file = storeFile.file();
        if (file.exists()) {
            if (file.delete()) {
                logger.debug("File {} deleted.", file.getAbsolutePath());
            } else {
                throw new IOException(String.format("Delete file %s failed!", file.getAbsolutePath()));
            }
        }
    }

    /**
     * 获得日志的相对给定位置的位置（怎么这么绕？）
     * 先找到position所在的日志的起始位置，然后：
     * 向前(offsetCount为负值时)/向后(offsetCount为正值时
     * 偏移offsetCount条日志，返回日志的位置
     * 如果到了左右边界，直接返回左右边界位置。
     *
     * @param position    给定的绝对位置
     * @param offsetCount 偏移日志条数，可以为负值
     */
    public long position(long position, int offsetCount) {

        int offset = 0;
        long pos = position;
        if (pos < left()) {
            pos = left();
        } else if (pos > right()) {
            pos = right();
        } else if (left() < pos && pos < right()) {
            pos = toLogStart(position);
        }

        if (offsetCount > 0) {
            while (offset < offsetCount && pos < right()) {
                pos = toLogTail(pos);
                offset++;
            }
        } else if (offsetCount < 0) {
            while (offset > offsetCount && pos > left()) {
                pos = toLogStart(pos - 1);
                offset--;
            }

        }

        return pos;
    }


    public File base() {
        return base;
    }

    @Override
    public void close() {
        for (StoreFile storeFile : storeFileMap.values()) {
            storeFile.unload();
        }
    }

    public byte[] readBytes(long position, int length) throws IOException {
        checkReadPosition(position);
        StoreFile storeFile = storeFileMap.floorEntry(position).getValue();
        int relPosition = (int) (position - storeFile.position());
        return storeFile.readByteBuffer(relPosition, length).array();
    }

    public int fileCount() {
        return storeFileMap.size();
    }

    public boolean meetMinStoreFile(long minIndexedPhysicalPosition) {
        return storeFileMap.headMap(minIndexedPhysicalPosition).size() > 0;
    }

    public boolean isEarly(long timestamp, long minIndexedPhysicalPosition) {
        for (StoreFile<T> storeFile : storeFileMap.headMap(minIndexedPhysicalPosition).values()) {
            if (storeFile.timestamp() > 0)
                if (storeFile.timestamp() < timestamp) {
                    return true;
                }
        }
        return false;
    }

    public static class Config {
        public static final int DEFAULT_FILE_HEADER_SIZE = 128;
        public static final int DEFAULT_FILE_DATA_SIZE = 128 * 1024 * 1024;

        /**
         * 文件头长度
         */
        private final int fileHeaderSize;
        /**
         * 文件内数据最大长度
         */
        private final int fileDataSize;

        public Config() {
            this(DEFAULT_FILE_DATA_SIZE,
                    DEFAULT_FILE_HEADER_SIZE);
        }

        public Config(int fileDataSize) {
            this(fileDataSize,
                    DEFAULT_FILE_HEADER_SIZE);
        }

        public Config(int fileDataSize, int fileHeaderSize) {
            this.fileDataSize = fileDataSize;
            this.fileHeaderSize = fileHeaderSize;
        }
    }


}
