package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.QKeyword;

import java.util.Date;

/**
 * Created by wangxiaofei1 on 2018/12/5.
 */
public class QRetry  extends QKeyword {

    private Date beginTime;

    private Date endTime;

    /**
     * 主题
     */
    private String topic;
    /**
     * 系统代码
     */
    private String app;
    /**
     * 业务ID
     */
    private String businessId;

    private Integer status;

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
