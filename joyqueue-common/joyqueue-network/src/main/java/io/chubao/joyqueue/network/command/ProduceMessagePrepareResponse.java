package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * ProduceMessagePrepareResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessagePrepareResponse extends JoyQueuePayload {

    private String txId;
    private JoyQueueCode code;

    public ProduceMessagePrepareResponse() {

    }

    public ProduceMessagePrepareResponse(JoyQueueCode code) {
        this.code = code;
    }

    public ProduceMessagePrepareResponse(String txId, JoyQueueCode code) {
        this.txId = txId;
        this.code = code;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_PREPARE_RESPONSE.getCode();
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}