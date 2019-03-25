package com.jd.journalq.client.internal.producer.domain;

import com.jd.journalq.common.exception.JMQCode;

/**
 * SendResultData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class SendResultData {

    private SendResult result;
    private JMQCode code;

    public SendResult getResult() {
        return result;
    }

    public void setResult(SendResult result) {
        this.result = result;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }
}