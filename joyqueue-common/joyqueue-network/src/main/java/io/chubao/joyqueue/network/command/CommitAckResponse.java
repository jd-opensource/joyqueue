package io.chubao.joyqueue.network.command;

import com.google.common.collect.Table;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * CommitAckRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckResponse extends JoyQueuePayload {

    private Table<String, Short, JoyQueueCode> result;

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_RESPONSE.getCode();
    }

    public void setResult(Table<String, Short, JoyQueueCode> result) {
        this.result = result;
    }

    public Table<String, Short, JoyQueueCode> getResult() {
        return result;
    }
}