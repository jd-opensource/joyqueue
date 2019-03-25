package com.jd.journalq.model.query;

import com.jd.journalq.model.QKeyword;
import com.jd.journalq.model.Query;

import java.util.Date;

/**
 * Created by wangxiaofei1 on 2019/3/5.
 */
public class QArchiveTask extends QKeyword implements Query {
    private String topic;
    private Date beginTime;
    private Date endTime;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

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
}
