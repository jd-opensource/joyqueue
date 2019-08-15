package io.chubao.joyqueue.model.domain;

/**
 * 某个App未订阅的主题
 * Created by chenyanying3 on 2018-10-17
 */
public class TopicUnsubscribedApplication extends Application {

    private String topicCode;
    private Boolean subscribeGroupExist;
    private int subscribeType;

    public TopicUnsubscribedApplication(Application app) {
        this.setCode(app.getCode());
        this.setId(app.getId());
        this.setAliasCode(app.getAliasCode());
        this.setSystem(app.getSystem());
        this.setOwner(app.getOwner());
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public Boolean isSubscribeGroupExist() {
        return subscribeGroupExist;
    }

    public void setSubscribeGroupExist(Boolean subscribeGroupExist) {
        this.subscribeGroupExist = subscribeGroupExist;
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(int subscribeType) {
        this.subscribeType = subscribeType;
    }
}