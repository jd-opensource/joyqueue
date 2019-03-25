package com.jd.journalq.common.network.command;

import com.jd.journalq.common.exception.JMQCode;

/**
 * CommitAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckResult {

    private JMQCode code;

    public CommitAckResult() {

    }

    public CommitAckResult(JMQCode code) {
        this.code = code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }
}