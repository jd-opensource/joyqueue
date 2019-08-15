package io.chubao.joyqueue.client.internal.consumer.domain;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * FetchIndexData
 *
 * author: gaohaoxiang
 * date: 2018/12/14
 */
public class FetchIndexData {

    private long index;
    private JoyQueueCode code;

    public FetchIndexData() {

    }

    public FetchIndexData(JoyQueueCode code) {
        this.code = code;
    }

    public FetchIndexData(long index, JoyQueueCode code) {
        this.index = index;
        this.code = code;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }
}