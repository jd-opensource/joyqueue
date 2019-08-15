package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * ProduceMessageCommitResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class ProduceMessageCommitResponse extends JoyQueuePayload {

    private JoyQueueCode code;

    public ProduceMessageCommitResponse() {

    }

    public ProduceMessageCommitResponse(JoyQueueCode code) {
        this.code = code;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_COMMIT_RESPONSE.getCode();
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}