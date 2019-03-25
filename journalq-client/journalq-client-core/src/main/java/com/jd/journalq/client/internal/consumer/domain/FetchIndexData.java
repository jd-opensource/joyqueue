package com.jd.journalq.client.internal.consumer.domain;

import com.jd.journalq.exception.JMQCode;

/**
 * FetchIndexData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/14
 */
public class FetchIndexData {

    private long index;
    private JMQCode code;

    public FetchIndexData() {

    }

    public FetchIndexData(JMQCode code) {
        this.code = code;
    }

    public FetchIndexData(long index, JMQCode code) {
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