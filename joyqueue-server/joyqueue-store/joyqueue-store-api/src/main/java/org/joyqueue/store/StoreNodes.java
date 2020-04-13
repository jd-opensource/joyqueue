package org.joyqueue.store;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * StoreNodes
 * author: gaohaoxiang
 * date: 2020/3/19
 */
public class StoreNodes {

    private List<StoreNode> nodes;

    public StoreNodes() {

    }

    public StoreNodes(StoreNode node) {
        this(Lists.newArrayList(node));
    }

    public StoreNodes(List<StoreNode> nodes) {
        this.nodes = nodes;
    }

    public StoreNode getRWNode() {
        for (StoreNode node : nodes) {
            if (node.isWritable() && node.isReadable()) {
                return node;
            }
        }
        return null;
    }

    public StoreNode getWritableNode() {
        for (StoreNode node : nodes) {
            if (node.isWritable()) {
                return node;
            }
        }
        return null;
    }

    public List<StoreNode> getReadableNodes() {
        List<StoreNode> result = Lists.newArrayListWithCapacity(nodes.size());
        for (StoreNode node : nodes) {
            if (node.isReadable()) {
                result.add(node);
            }
        }
        return result;
    }

    public List<StoreNode> getNodes() {
        return nodes;
    }
}