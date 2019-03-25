package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/2/21
 */
public class AddTopic extends JMQPayload {
    private Topic topic;
    private List<PartitionGroup> partitionGroups;

    public AddTopic topic(Topic topic){
        this.topic = topic;
        return this;
    }
    public AddTopic partitiionGroups(List<PartitionGroup> partitionGroups){
        this.partitionGroups = partitionGroups;
        return this;
    }

    public Topic getTopic() {
        return topic;
    }

    public List<PartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    @Override
    public int type() {
        return NsrCommandType.ADD_TOPIC;
    }
}
