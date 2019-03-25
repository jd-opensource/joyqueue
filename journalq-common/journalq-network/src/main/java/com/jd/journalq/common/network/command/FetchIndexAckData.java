package com.jd.journalq.common.network.command;

import com.jd.journalq.common.exception.JMQCode;

/**
 * FetchIndexAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexAckData {

    private long index;
    private JMQCode code;

    public FetchIndexAckData() {

    }

    public FetchIndexAckData(long index, JMQCode code) {
        this.index = index;
        this.code = code;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }
}