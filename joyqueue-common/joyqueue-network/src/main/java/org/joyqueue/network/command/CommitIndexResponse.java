package org.joyqueue.network.command;

import com.google.common.collect.Table;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * CommitIndexRequest
 * author: gaohaoxiang
 * date: 2020/5/20
 */
public class CommitIndexResponse extends JoyQueuePayload {

    private Table<String, Short, JoyQueueCode> result;

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_INDEX_RESPONSE.getCode();
    }

    public void setResult(Table<String, Short, JoyQueueCode> result) {
        this.result = result;
    }

    public Table<String, Short, JoyQueueCode> getResult() {
        return result;
    }
}