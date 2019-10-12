package io.chubao.joyqueue.domain;

public class BrokerPartitionGroupLeaderInfo {
    private int partitionGroups;
    private int leaders;

    public int getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(int partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    public int getLeaders() {
        return leaders;
    }

    public void setLeaders(int leaders) {
        this.leaders = leaders;
    }
}
