package io.chubao.joyqueue.network.command;

import com.google.common.collect.Table;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchPartitionMessageResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchPartitionMessageResponse extends JoyQueuePayload {

    private Table<String, Short, FetchPartitionMessageAckData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PARTITION_MESSAGE_RESPONSE.getCode();
    }

    public Table<String, Short, FetchPartitionMessageAckData> getData() {
        return data;
    }

    public void setData(Table<String, Short, FetchPartitionMessageAckData> data) {
        this.data = data;
    }
}