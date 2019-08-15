package io.chubao.joyqueue.client.internal.producer.domain;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.util.List;

/**
 * SendBatchResultData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class SendBatchResultData {

    private List<SendResult> result;
    private JoyQueueCode code;

    public List<SendResult> getResult() {
        return result;
    }

    public void setResult(List<SendResult> result) {
        this.result = result;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}