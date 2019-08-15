package io.chubao.joyqueue.network.command;

import com.google.common.collect.Table;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * CommitAckRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckRequest extends JoyQueuePayload {

    private Table<String, Short, List<CommitAckData>> data;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_REQUEST.getCode();
    }

    public void setData(Table<String, Short, List<CommitAckData>> data) {
        this.data = data;
    }

    public Table<String, Short, List<CommitAckData>> getData() {
        return data;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}