package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/2/21
 */
public class AddTopic extends JoyQueuePayload {
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
