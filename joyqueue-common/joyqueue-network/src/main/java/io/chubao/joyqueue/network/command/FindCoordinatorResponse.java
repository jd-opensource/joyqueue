package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * FindCoordinatorResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class FindCoordinatorResponse extends JoyQueuePayload {

    private Map<String, FindCoordinatorAckData> coordinators;

    @Override
    public int type() {
        return JoyQueueCommandType.FIND_COORDINATOR_RESPONSE.getCode();
    }

    public void setCoordinators(Map<String, FindCoordinatorAckData> coordinators) {
        this.coordinators = coordinators;
    }

    public Map<String, FindCoordinatorAckData> getCoordinators() {
        return coordinators;
    }
}