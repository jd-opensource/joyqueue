package org.joyqueue.client.internal.metadata.domain;

/**
 * PartitionNode
 * author: gaohaoxiang
 * date: 2020/8/11
 */
public class PartitionNode {

    private PartitionMetadata partitionMetadata;

    public PartitionNode(PartitionMetadata partitionMetadata) {
        this.partitionMetadata = partitionMetadata;
    }

    public PartitionNodeTracer begin() {
        return new PartitionNodeTracer();
    }

    public PartitionMetadata getPartitionMetadata() {
        return partitionMetadata;
    }

    public static class PartitionNodeTracer {

        public void end() {

        }

        public void error() {

        }
    }
}