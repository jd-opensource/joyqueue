package org.joyqueue.broker.network.command;

import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;
import java.util.Map;

/**
 * GetPartitionGroupClusterRequest
 * author: gaohaoxiang
 * date: 2020/3/19
 */
public class GetPartitionGroupClusterRequest extends JoyQueuePayload {

    private Map<String, List<Integer>> groups;

    public void setGroups(Map<String, List<Integer>> groups) {
        this.groups = groups;
    }

    public Map<String, List<Integer>> getGroups() {
        return groups;
    }

    @Override
    public int type() {
        return CommandType.GET_PARTITION_GROUP_CLUSTER_REQUEST;
    }
}