package org.joyqueue.monitor;

import java.util.List;

/**
 * BrokerMonitorInfoExt
 * author: gaohaoxiang
 * date: 2020/11/23
 */
public class BrokerMonitorInfoExt extends BrokerMonitorInfo {

    private List<TopicMonitorInfo> topics;
    private List<TopicMonitorInfo> appTopics;
    private List<PartitionGroupMonitorInfo> partitionGroups;
    private List<PartitionGroupMonitorInfo> appPartitionGroups;
    private List<PartitionMonitorInfo> partitions;
    private List<PartitionMonitorInfo> appPartitions;
    private ArchiveMonitorInfo archive;

    public List<TopicMonitorInfo> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicMonitorInfo> topics) {
        this.topics = topics;
    }

    public List<TopicMonitorInfo> getAppTopics() {
        return appTopics;
    }

    public void setAppTopics(List<TopicMonitorInfo> appTopics) {
        this.appTopics = appTopics;
    }

    public List<PartitionGroupMonitorInfo> getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(List<PartitionGroupMonitorInfo> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    public List<PartitionGroupMonitorInfo> getAppPartitionGroups() {
        return appPartitionGroups;
    }

    public void setAppPartitionGroups(List<PartitionGroupMonitorInfo> appPartitionGroups) {
        this.appPartitionGroups = appPartitionGroups;
    }

    public List<PartitionMonitorInfo> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<PartitionMonitorInfo> partitions) {
        this.partitions = partitions;
    }

    public List<PartitionMonitorInfo> getAppPartitions() {
        return appPartitions;
    }

    public void setAppPartitions(List<PartitionMonitorInfo> appPartitions) {
        this.appPartitions = appPartitions;
    }

    public ArchiveMonitorInfo getArchive() {
        return archive;
    }

    public void setArchive(ArchiveMonitorInfo archive) {
        this.archive = archive;
    }
}