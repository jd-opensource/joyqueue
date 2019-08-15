package io.chubao.joyqueue.network.command;

import com.google.common.collect.Table;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * FetchIndexResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexResponse extends JoyQueuePayload {

    private Table<String, Short, FetchIndexAckData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_INDEX_RESPONSE.getCode();
    }

    public void setData(Table<String, Short, FetchIndexAckData> data) {
        this.data = data;
    }

    public Table<String, Short, FetchIndexAckData> getData() {
        return data;
    }
}