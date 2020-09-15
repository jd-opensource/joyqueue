package org.joyqueue.network.command;

import com.google.common.collect.Table;
import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * CommitIndexRequest
 * author: gaohaoxiang
 * date: 2020/5/20
 */
public class CommitIndexRequest extends JoyQueuePayload {

    public static final long MAX_INDEX = -1;

    public static final long MIN_INDEX = -2;

    private Table<String, Short, Long> data;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_INDEX_REQUEST.getCode();
    }

    public Table<String, Short, Long> getData() {
        return data;
    }

    public void setData(Table<String, Short, Long> data) {
        this.data = data;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}