package org.joyqueue.broker.cluster.entry;

/**
 * ClusterNode
 * author: gaohaoxiang
 * date: 2020/3/23
 */
public class ClusterNode {

    private int leader;

    public ClusterNode() {

    }

    public ClusterNode(int leader) {
        this.leader = leader;
    }

    public int getLeader() {
        return leader;
    }

    public void setLeader(int leader) {
        this.leader = leader;
    }

    @Override
    public String toString() {
        return "ClusterNode{" +
                "leader=" + leader +
                '}';
    }
}