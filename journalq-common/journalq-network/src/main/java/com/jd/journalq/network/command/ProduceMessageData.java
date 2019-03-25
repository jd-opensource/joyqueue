package com.jd.journalq.network.command;

import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.message.BrokerMessage;

import java.util.List;

/**
 * ProduceMessageData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
}