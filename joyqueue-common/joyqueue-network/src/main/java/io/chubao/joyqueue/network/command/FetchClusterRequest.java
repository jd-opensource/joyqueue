package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * GetCluster
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class FetchClusterRequest extends JoyQueuePayload {

    private List<String> topics;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_CLUSTER_REQUEST.getCode();
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}