package io.chubao.joyqueue.client.internal.producer.domain;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * SendResultData
 *
 * author: gaohaoxiang
 * date: 2018/12/20
 */
public class SendResultData {

    private SendResult result;
    private JoyQueueCode code;

    public SendResult getResult() {
        return result;
    }

    public void setResult(SendResult result) {
        this.result = result;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}