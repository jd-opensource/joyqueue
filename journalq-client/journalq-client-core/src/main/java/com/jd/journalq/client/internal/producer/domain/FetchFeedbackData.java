package com.jd.journalq.client.internal.producer.domain;

import com.jd.journalq.common.exception.JMQCode;

import java.util.List;

/**
 * FetchFeedbackData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/24
 */
public class FetchFeedbackData {

    private List<FeedbackData> data;
    private JMQCode code;

    public List<FeedbackData> getData() {
        return data;
    }

    public void setData(List<FeedbackData> data) {
        this.data = data;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }
}