package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * FindCoordinatorResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorResponse extends JMQPayload {

    private Map<String, FindCoordinatorAckData> coordinators;

    @Override
    public int type() {
        return JMQCommandType.FIND_COORDINATOR_ACK.getCode();
    }

    public void setCoordinators(Map<String, FindCoordinatorAckData> coordinators) {
        this.coordinators = coordinators;
    }

    public Map<String, FindCoordinatorAckData> getCoordinators() {
        return coordinators;
    }
}