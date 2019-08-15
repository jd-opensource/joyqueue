package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * CommitAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckResult {

    private JoyQueueCode code;

    public CommitAckResult() {

    }

    public CommitAckResult(JoyQueueCode code) {
        this.code = code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}