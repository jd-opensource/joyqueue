package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * FindCoordinatorRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class FindCoordinatorRequest extends JoyQueuePayload {

    private List<String> topics;
    private String app;

    @Override
    public int type() {
        return JoyQueueCommandType.FIND_COORDINATOR_REQUEST.getCode();
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