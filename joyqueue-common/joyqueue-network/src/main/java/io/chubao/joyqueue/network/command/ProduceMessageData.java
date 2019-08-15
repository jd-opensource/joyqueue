package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.message.BrokerMessage;

import java.util.List;

/**
 * ProduceMessageData
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageData {

    private String txId;
    private int timeout;
    private QosLevel qosLevel;
    private List<BrokerMessage> messages;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setQosLevel(QosLevel qosLevel) {
        this.qosLevel = qosLevel;
    }

    public QosLevel getQosLevel() {
        return qosLevel;
    }

    public List<BrokerMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<BrokerMessage> messages) {
        this.messages = messages;
    }

    public int getSize() {
        if (messages == null) {
            return 0;
        }
        int size = 0;
        for (BrokerMessage message : messages) {
            size += message.getSize();
        }
        return size;
    }
}