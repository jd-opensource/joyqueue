package org.joyqueue.store.journalkeeper;

import io.journalkeeper.core.monitor.SimpleMonitorCollector;
import io.journalkeeper.monitor.JournalMonitorInfo;
import io.journalkeeper.monitor.JournalPartitionMonitorInfo;
import io.journalkeeper.monitor.MonitorCollector;
import io.journalkeeper.monitor.MonitoredServer;
import io.journalkeeper.monitor.ServerMonitorInfo;
import io.journalkeeper.utils.spi.ServiceSupport;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.ReadResult;
import org.joyqueue.store.StoreManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author LiYue
 * Date: 2019/11/29
 */
public class ManagementServiceImpl implements StoreManagementService {
    private static final Logger logger = LoggerFactory.getLogger(ManagementServiceImpl.class);
    private final Map<JournalKeeperStore.TopicPartitionGroup, JournalKeeperPartitionGroupStore> storeMap;
    private final SimpleMonitorCollector monitorCollector;
    public ManagementServiceImpl(Map<JournalKeeperStore.TopicPartitionGroup, JournalKeeperPartitionGroupStore> storeMap) {
        this.storeMap = storeMap;
        monitorCollector = ServiceSupport.load(MonitorCollector.class, SimpleMonitorCollector.class.getCanonicalName());
    }

    @Override
    public TopicMetric[] storeMetrics() {
        return storeMap.keySet().stream().map(JournalKeeperStore.TopicPartitionGroup::getTopic)
                .distinct().map(this::topicMetric).toArray(TopicMetric[]::new);
    }

    @Override
    public TopicMetric topicMetric(String topic) {
        TopicMetric topicMetric = new TopicMetric();
        topicMetric.setTopic(topic);
        topicMetric.setPartitionGroupMetrics(
                storeMap.entrySet().stream().map(entry -> {
                    JournalKeeperStore.TopicPartitionGroup group = entry.getKey();
                    JournalKeeperPartitionGroupStore store = entry.getValue();
                    return getPartitionGroupMetric(group, store);
                }).filter(Objects::nonNull).toArray(PartitionGroupMetric[]::new)
        );
        return topicMetric;
    }

    @Override
    public PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup) {
        JournalKeeperStore.TopicPartitionGroup group = new JournalKeeperStore.TopicPartitionGroup(topic, partitionGroup);
        JournalKeeperPartitionGroupStore store = storeMap.get(group);
        return  getPartitionGroupMetric(group, store);
    }

    private PartitionGroupMetric getPartitionGroupMetric(JournalKeeperStore.TopicPartitionGroup group, JournalKeeperPartitionGroupStore store) {
        if (null != store) {
            MonitoredServer monitoredServer = monitorCollector.getMonitoredServer(store.getUri());
            if (null != monitoredServer) {
                ServerMonitorInfo info = monitoredServer.collect();
                return toPartitionGroupMetric(info, group.getPartitionGroup());
            }
        }
        return null;
    }

    private PartitionGroupMetric toPartitionGroupMetric(ServerMonitorInfo info, int partitionGroup) {
        PartitionGroupMetric partitionGroupMetric = new PartitionGroupMetric();
        partitionGroupMetric.setPartitionGroup(partitionGroup);
        JournalMonitorInfo journalMonitorInfo = info.getJournal();
        partitionGroupMetric.setFlushPosition(journalMonitorInfo.getFlushIndex());
        partitionGroupMetric.setLeftPosition(journalMonitorInfo.getMinIndex());
        partitionGroupMetric.setRightPosition(journalMonitorInfo.getMaxIndex());
        partitionGroupMetric.setReplicationPosition(journalMonitorInfo.getCommitIndex());
        partitionGroupMetric.setPartitionMetrics(
                info.getJournal().getPartitions().stream().map(this::toPartitionMetric).toArray(PartitionMetric[]::new)
        );
        partitionGroupMetric.setStorageSize(journalMonitorInfo.getUsedSpace());
        return partitionGroupMetric;
    }

    @Override
    public PartitionMetric partitionMetric(String topic, short partition) {
        JournalKeeperStore.TopicPartitionGroup group = getStore(topic, partition);
        if(null != group) {
            JournalKeeperPartitionGroupStore store = storeMap.get(group);
            if (null != store) {
                MonitoredServer monitoredServer = monitorCollector.getMonitoredServer(store.getUri());
                if(null != monitoredServer) {
                    ServerMonitorInfo info = monitoredServer.collect();
                    for (JournalPartitionMonitorInfo partitionMonitorInfo : info.getJournal().getPartitions()) {
                        if (partitionMonitorInfo.getPartition() == (int) partition) {
                            return toPartitionMetric(partitionMonitorInfo);
                        }
                    }
                }
            }
        }
        return null;
    }

    private PartitionMetric toPartitionMetric(JournalPartitionMonitorInfo partitionMonitorInfo) {
        PartitionMetric partitionMetric = new PartitionMetric();
        partitionMetric.setPartition((short ) partitionMonitorInfo.getPartition());
        partitionMetric.setLeftIndex(partitionMonitorInfo.getMinIndex());
        partitionMetric.setRightIndex(partitionMonitorInfo.getMaxIndex());
        return partitionMetric;
    }

    @Override
    public File[] listFiles(String path) {
        throw new UnsupportedOperationException();    }

    @Override
    public File[] listFiles(File directory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long totalSpace() {
        throw new UnsupportedOperationException();    }

    @Override
    public long freeSpace() {
        throw new UnsupportedOperationException();    }

    @Override
    public byte[][] readMessages(String topic, int partitionGroup, long position, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[][] readMessages(String topic, short partition, long index, int count) {
        JournalKeeperStore.TopicPartitionGroup group = getStore(topic, partition);
        if(null != group) {
            JournalKeeperPartitionGroupStore store = storeMap.get(group);
            if(null != store) {
                ReadResult readResult = store.read(partition, index, count, 0);
                if (readResult.getCode() == JoyQueueCode.SUCCESS) {
                    return Arrays.stream(readResult.getMessages()).map(ByteBuffer::array).toArray(byte[][]::new);
                } else {
                    logger.warn("Read failed! topic: {}, partition: {}, index: {}, count: {}, err: {}.",
                            topic, partition, index, count,readResult.getCode()
                    );
                }

            }
        }
        return new byte[0][];
    }

    private JournalKeeperStore.TopicPartitionGroup getStore(String topic, short partition) {
        for (Map.Entry<JournalKeeperStore.TopicPartitionGroup, JournalKeeperPartitionGroupStore> entry : storeMap.entrySet()) {
            JournalKeeperStore.TopicPartitionGroup group = entry.getKey();
            if (group.getTopic().equals(topic)) {
                JournalKeeperPartitionGroupStore store = entry.getValue();
                Short[] partitions = store.listPartitions();
                if (Arrays.stream(partitions).anyMatch(p -> p == partition)) {
                    return group;
                }
            }
        }
        return null;
    }

    @Override
    public byte[][] readMessages(File file, long position, int count, boolean includeFileHeader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long[] readIndices(String topic, short partition, long index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long[] readIndices(File file, long position, int count, boolean includeFileHeader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] readFile(File file, long position, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] readPartitionGroupStore(String topic, int partitionGroup, long position, int length) {
        throw new UnsupportedOperationException();
    }
}
