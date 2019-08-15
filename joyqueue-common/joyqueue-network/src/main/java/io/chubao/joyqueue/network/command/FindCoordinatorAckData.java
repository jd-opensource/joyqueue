package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.domain.BrokerNode;

/**
 * FindCoordinatorAckData
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class FindCoordinatorAckData {

    private BrokerNode node;
    private JoyQueueCode code;

    public FindCoordinatorAckData() {

    }

    public FindCoordinatorAckData(JoyQueueCode code) {
        this.code = code;
    }

    public FindCoordinatorAckData(BrokerNode node, JoyQueueCode code) {
        this.node = node;
        this.code = code;
    }

    public BrokerNode getNode() {
        return node;
    }

    public void setNode(BrokerNode node) {
        this.node = node;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}