package io.chubao.joyqueue.network.command;

import com.google.common.collect.Table;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchPartitionMessageRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchPartitionMessageRequest extends JoyQueuePayload {

    public static final long NONE_INDEX = -1;

    private Table<String, Short, FetchPartitionMessageData> partitions;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PARTITION_MESSAGE_REQUEST.getCode();
    }

    public void setPartitions(Table<String, Short, FetchPartitionMessageData> partitions) {
        this.partitions = partitions;
    }

    public Table<String, Short, FetchPartitionMessageData> getPartitions() {
        return partitions;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}