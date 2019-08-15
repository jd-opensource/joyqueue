package io.chubao.joyqueue.client.internal.producer.domain;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.util.List;

/**
 * FetchFeedbackData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/24
 */
public class FetchFeedbackData {

    private List<FeedbackData> data;
    private JoyQueueCode code;

    public List<FeedbackData> getData() {
        return data;
    }

    public void setData(List<FeedbackData> data) {
        this.data = data;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}