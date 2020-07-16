package org.joyqueue.broker.network.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;
import java.util.Map;

/**
 * GetPartitionGroupClusterResponse
 * author: gaohaoxiang
 * date: 2020/3/19
 */
public class GetPartitionGroupClusterResponse extends JoyQueuePayload {

    private Map<String, Map<Integer, PartitionGroupCluster>> groups;

    public void setGroups(Map<String, Map<Integer, PartitionGroupCluster>> groups) {
        this.groups = groups;
    }

    public Map<String, Map<Integer, PartitionGroupCluster>> getGroups() {
        return groups;
    }

    public PartitionGroupCluster getCluster(String topic, int group) {
        if (groups == null) {
            return null;
        }
        Map<Integer, PartitionGroupCluster> topicMap = groups.get(topic);
        if (topicMap == null) {
            return null;
        }
        return topicMap.get(group);
    }

    public void addCluster(String topic, int group, PartitionGroupCluster cluster) {
        if (groups == null) {
            groups = Maps.newHashMap();
        }
        Map<Integer, PartitionGroupCluster> topicMap = groups.get(topic);
        if (topicMap == null) {
            topicMap = Maps.newHashMap();
            groups.put(topic, topicMap);
        }
        topicMap.put(group, cluster);
    }

    @Override
    public int type() {
        return CommandType.GET_PARTITION_GROUP_CLUSTER_RESPONSE;
    }

    public static class PartitionGroupCluster {

        private List<PartitionGroupNode> nodes;

        public void addNode(PartitionGroupNode node) {
            if (nodes == null) {
                nodes = Lists.newLinkedList();
            }
            nodes.add(node);
        }

        public PartitionGroupNode getRWNode() {
            for (PartitionGroupNode node : nodes) {
                if (node.isWritable() && node.isReadable()) {
                    return node;
                }
            }
            return null;
        }

        public PartitionGroupNode getWritableNode() {
            for (PartitionGroupNode node : nodes) {
                if (node.isWritable()) {
                    return node;
                }
            }
            return null;
        }

        public List<PartitionGroupNode> getReadableNodes() {
            List<PartitionGroupNode> result = Lists.newArrayListWithCapacity(nodes.size());
            for (PartitionGroupNode node : nodes) {
                if (node.isReadable()) {
                    result.add(node);
                }
            }
            return result;
        }

        public List<PartitionGroupNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<PartitionGroupNode> nodes) {
            this.nodes = nodes;
        }
    }

    public static class PartitionGroupNode {
        private int id;
        private boolean writable;
        private boolean readable;

        public PartitionGroupNode() {

        }

        public PartitionGroupNode(int id, boolean writable, boolean readable) {
            this.id = id;
            this.writable = writable;
            this.readable = readable;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isWritable() {
            return writable;
        }

        public void setWritable(boolean writable) {
            this.writable = writable;
        }

        public boolean isReadable() {
            return readable;
        }

        public void setReadable(boolean readable) {
            this.readable = readable;
        }
    }
}