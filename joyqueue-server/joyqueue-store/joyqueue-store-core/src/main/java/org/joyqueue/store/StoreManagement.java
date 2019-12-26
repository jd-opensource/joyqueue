/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.store;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.store.file.StoreFile;
import org.joyqueue.store.index.IndexItem;
import org.joyqueue.store.utils.ByteBufferUtils;
import org.joyqueue.store.utils.PreloadBufferPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author liyue25
 * Date: 2018/10/18
 */
public class StoreManagement implements StoreManagementService {

    private static final Logger logger = LoggerFactory.getLogger(StoreManagement.class);
    private final int messageFileHeaderSize, indexFileHeaderSize, maxMessageSize;
    private final PreloadBufferPool bufferPool;
    private final Store store;

    public StoreManagement(int messageFileHeaderSize, int indexFileHeaderSize, int maxMessageSize, PreloadBufferPool bufferPool, Store store) {
        this.messageFileHeaderSize = messageFileHeaderSize;
        this.indexFileHeaderSize = indexFileHeaderSize;
        this.maxMessageSize = maxMessageSize;
        this.bufferPool = bufferPool;
        this.store = store;
    }


    /**
     * 获取Store度量信息
     */
    @Override
    public TopicMetric[] storeMetrics() {
        return store.topics().stream()
                .map(this::topicMetric).toArray(TopicMetric[]::new);
    }

    @Override
    public PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup) {

        PartitionGroupMetric partitionGroupMetric = null;
        PartitionGroupStoreManager partitionGroupStoreManger = store.partitionGroupStore(topic, partitionGroup);
        if (null != partitionGroupStoreManger) {
            partitionGroupMetric = new PartitionGroupMetric();
            partitionGroupMetric.setPartitionGroup(partitionGroup);
            partitionGroupMetric.setFlushPosition(partitionGroupStoreManger.flushPosition());
            partitionGroupMetric.setIndexPosition(partitionGroupStoreManger.indexPosition());
            partitionGroupMetric.setLeftPosition(partitionGroupStoreManger.leftPosition());
            partitionGroupMetric.setReplicationPosition(partitionGroupStoreManger.commitPosition());
            partitionGroupMetric.setRightPosition(partitionGroupStoreManger.rightPosition());

            partitionGroupMetric.setPartitionMetrics(
                    Arrays.stream(partitionGroupStoreManger.listPartitions())
                            .map(partition -> getPartitionMetric(partitionGroupStoreManger, partition))
                            .filter(Objects::nonNull)
                            .toArray(PartitionMetric[]::new)
            );
        }
        return partitionGroupMetric;
    }

    private PartitionMetric getPartitionMetric(PartitionGroupStoreManager partitionGroupStoreManger, short partition) {
        PartitionMetric partitionMetric = null;

        PositioningStore<IndexItem> indexStore = partitionGroupStoreManger.indexStore(partition);
        if (null != indexStore) {
            partitionMetric = new PartitionMetric();
            partitionMetric.setPartition(partition);

            partitionMetric.setLeftIndex(indexStore.left() / IndexItem.STORAGE_SIZE);
            partitionMetric.setRightIndex(indexStore.right() / IndexItem.STORAGE_SIZE);
        }

        return partitionMetric;
    }

    @Override
    public TopicMetric topicMetric(String topic) {
        TopicMetric topicMetric = new TopicMetric();
        topicMetric.setTopic(topic);

        topicMetric.setPartitionGroupMetrics(
                store.partitionGroups(topic).stream()
                        .map(partitionGroup -> partitionGroupMetric(topic, partitionGroup))
                        .filter(Objects::nonNull)
                        .toArray(PartitionGroupMetric[]::new)
        );
        return topicMetric;
    }


    @Override
    public PartitionMetric partitionMetric(String topic, short partition) {
        return store.partitionGroups(topic).stream()
                .map(g -> store.partitionGroupStore(topic, g))
                .filter(Objects::nonNull)
                .map(partitionGroupStoreManger -> getPartitionMetric(partitionGroupStoreManger, partition))
                .filter(Objects::nonNull).findAny().orElse(null);
    }

    /**
     * 列出store中给定path的所有文件
     *
     * @param path 相对store根目录的相对路径
     */
    @Override
    public File[] listFiles(String path) {
        File dir = new File(path);
        if (!dir.isAbsolute()) {
            dir = new File(store.base(), path);
        }


        return listFiles(dir);
    }

    @Override
    public File[] listFiles(File directory) {
        return directory.listFiles();
    }

    public long freeSpace() {
        return store.base().getFreeSpace();
    }

    public long totalSpace(){
        return store.base().getTotalSpace();
    }

    private long usableSpace(){
        return store.base().getUsableSpace();
    }


    /**
     * 读取消息
     *
     * @param topic
     * @param partitionGroup
     * @param position
     * @param count
     */
    @Override
    public byte[][] readMessages(String topic, int partitionGroup, long position, int count) {
        try {
            return store.partitionGroupStore(topic, partitionGroup)
                    .messageStore().batchRead(position, count)
                    .stream()
                    .map(ByteBuffer::array)
                    .toArray(byte[][]::new);
        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }
    }

    private byte[] getBytes(ByteBuffer b) {
        byte[] bytes = new byte[b.capacity()];
        ByteBuffer wrappedBuffer = ByteBuffer.wrap(bytes);
        ByteBufferUtils.copy(b, wrappedBuffer);
        return bytes;
    }

    @Override
    public byte[][] readMessages(String topic, short partition, long index, int count) {
        try {
            return store.partitionGroups(topic).stream()
                    .map(g -> store.partitionGroupStore(topic, g))
                    .filter(Objects::nonNull)
                    .filter(s -> Arrays.stream(s.listPartitions()).anyMatch(p -> p == partition))
                    .map(s -> {
                        try {
                            return s.read(partition, index, count, Long.MAX_VALUE);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(r -> r.getCode() == JoyQueueCode.SUCCESS)
                    .map(ReadResult::getMessages)
                    .flatMap(Arrays::stream)
                    .map(this::getBytes)
                    .toArray(byte[][]::new);
        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }

    }

    @Override
    public byte[][] readMessages(File file, long position, int count, boolean includeFileHeader) {

        try {
            //TODO
            StoreFile<ByteBuffer> storeFile = null;//new StoreFileImpl<>(0, file, messageFileHeaderSize,
            //  new StoreMessageSerializer(maxMessageSize), bufferPool,  (int) file.length() - messageFileHeaderSize,2048);
            List<byte[]> messages = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                ByteBuffer byteBuffer = storeFile.read((int) position, -1);
                if (null != byteBuffer) {
                    position += byteBuffer.remaining() + 1;
                    messages.add(getBytes(byteBuffer));
                } else break;

            }
            return messages.toArray(new byte[0][]);


        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }
    }

    /**
     * 读取索引
     *
     * @param topic
     * @param partition
     * @param index
     * @param count
     */
    @Override
    public Long[] readIndices(String topic, short partition, long index, int count) {
        try {
            return store.partitionGroups(topic).stream()
                    .map(g -> store.partitionGroupStore(topic, g))
                    .filter(Objects::nonNull)
                    .filter(s -> Arrays.stream(s.listPartitions()).anyMatch(p -> p == partition))
                    .map(s -> s.indexStore(partition))
                    .map(is -> {
                        try {
                            return is.batchRead(index, count);
                        } catch (Exception e) {
                            logger.warn("Exception: ", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(IndexItem::getOffset)
                    .toArray(Long[]::new);

        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }

    }

    @Override
    public Long[] readIndices(File file, long position, int count, boolean includeFileHeader) {
        List<Long> retList = new ArrayList<>(count);
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel fileChannel = raf.getChannel()) {
            position += (includeFileHeader ? 0 : indexFileHeaderSize);
            for (int i = 0; i < count && position + (i + 1) * IndexItem.STORAGE_SIZE <= raf.length(); i++) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(IndexItem.STORAGE_SIZE);
                fileChannel.read(byteBuffer);
                retList.add(IndexItem.parseMessage(byteBuffer, 0L).getOffset());
            }
            return retList.toArray(new Long[0]);
        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }
    }

    /**
     * 裸接口
     *
     * @param file
     * @param position
     * @param length
     */
    @Override
    public byte[] readFile(File file, long position, int length) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            byte[] retBytes = new byte[length];
            raf.read(retBytes);
            return retBytes;
        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }
    }

    @Override
    public byte[] readPartitionGroupStore(String topic, int partitionGroup, long position, int length) {
        try {
            PositioningStore<ByteBuffer> messageStore = store.partitionGroupStore(topic, partitionGroup).messageStore();
            return messageStore.readBytes(position, length);
        } catch (Throwable t) {
            logger.warn("Exception:", t);
            return null;
        }
    }
}
