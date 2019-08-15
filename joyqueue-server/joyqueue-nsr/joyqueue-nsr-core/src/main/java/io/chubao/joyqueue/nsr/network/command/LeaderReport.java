package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class LeaderReport extends JoyQueuePayload {
    private TopicName topic;
    private int partitionGroup;
    private int leaderBrokerId;
    private Set<Integer> isrId;
    private int termId;

    public LeaderReport topic(TopicName topic){
        this.topic = topic;
        return this;
    }
    public LeaderReport partitionGroup(int partitionGroup){
        this.partitionGroup = partitionGroup;
        return this;
    }
    public LeaderReport leaderBrokerId(int leaderBrokerId){
        this.leaderBrokerId = leaderBrokerId;
        return this;
    }
    public LeaderReport isrId(Set<Integer> isrId){
        this.isrId = isrId;
        return this;
    }
    public LeaderReport termId(int termId){
        this.termId = termId;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public int getLeaderBrokerId() {
        return leaderBrokerId;
    }

    public Set<Integer> getIsrId() {
        return isrId;
    }

    public int getTermId() {
        return termId;
    }

    @Override
    public int type() {
        return NsrCommandType.LEADER_REPORT;
    }

    @Override
    public String toString() {
        return "LeaderReport{" +
                "topic=" + topic +
                ", partitionGroup=" + partitionGroup +
                ", leaderBrokerId=" + leaderBrokerId +
                ", isrId=" + isrId +
                ", termId=" + termId +
                '}';
    }
}
