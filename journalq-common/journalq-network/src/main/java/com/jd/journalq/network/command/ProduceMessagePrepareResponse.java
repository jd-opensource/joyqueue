package com.jd.journalq.network.command;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * ProduceMessagePrepareResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessagePrepareResponse extends JMQPayload {

    private String txId;
    private JMQCode code;

    public ProduceMessagePrepareResponse() {

    }

    public ProduceMessagePrepareResponse(JMQCode code) {
        this.code = code;
    }

    public ProduceMessagePrepareResponse(String txId, JMQCode code) {
        this.txId = txId;
        this.code = code;
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE_ACK.getCode();
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}