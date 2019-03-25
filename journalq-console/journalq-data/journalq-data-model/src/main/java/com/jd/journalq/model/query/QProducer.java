package com.jd.journalq.model.query;

import com.jd.journalq.common.model.QKeyword;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.Topic;

import java.util.List;

public class QProducer extends QKeyword implements QIdentity {
    private Topic topic;
    private Identity app;
    private List<String> appList;

    public QProducer() {

    }

    public QProducer(Topic topic) {
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Identity getApp() {
        return app;
    }

    public void setApp(Identity app) {
        this.app = app;
    }

    public List<String> getAppList() {
        return appList;
    }

    public void setAppList(List<String> appList) {
        this.appList = appList;
    }
}
