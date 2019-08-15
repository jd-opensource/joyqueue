package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * ProduceMessageRollbackResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageRollbackResponse extends JoyQueuePayload {

    private JoyQueueCode code;

    public ProduceMessageRollbackResponse() {

    }

    public ProduceMessageRollbackResponse(JoyQueueCode code) {
        this.code = code;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_ROLLBACK_RESPONSE.getCode();
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}