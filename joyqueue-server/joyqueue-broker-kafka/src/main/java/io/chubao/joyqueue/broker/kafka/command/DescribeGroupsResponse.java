package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.coordinator.group.domain.GroupDescribe;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;

/**
 * Created by zhuduohui on 2018/5/17.
 */
public class DescribeGroupsResponse extends KafkaRequestOrResponse {

    private List<GroupDescribe> groups;

    public DescribeGroupsResponse() {

    }

    public DescribeGroupsResponse(List<GroupDescribe> groups) {
        this.groups = groups;
    }

    public List<GroupDescribe> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDescribe> groups) {
        this.groups = groups;
    }

    @Override
    public int type() {
        return KafkaCommandType.DESCRIBE_GROUP.getCode();
    }
}
