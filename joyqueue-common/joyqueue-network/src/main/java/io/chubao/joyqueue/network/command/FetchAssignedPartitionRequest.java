package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * FetchAssignedPartitionRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class FetchAssignedPartitionRequest extends JoyQueuePayload {

    private List<FetchAssignedPartitionData> data;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_ASSIGNED_PARTITION_REQUEST.getCode();
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setData(List<FetchAssignedPartitionData> data) {
        this.data = data;
    }

    public List<FetchAssignedPartitionData> getData() {
        return data;
    }
}