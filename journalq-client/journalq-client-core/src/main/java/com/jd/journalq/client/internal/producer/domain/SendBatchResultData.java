package com.jd.journalq.client.internal.producer.domain;

import com.jd.journalq.common.exception.JMQCode;

import java.util.List;

/**
 * SendBatchResultData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class SendBatchResultData {

    private List<SendResult> result;
    private JMQCode code;

    public List<SendResult> getResult() {
        return result;
    }

    public void setResult(List<SendResult> result) {
        this.result = result;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }
}