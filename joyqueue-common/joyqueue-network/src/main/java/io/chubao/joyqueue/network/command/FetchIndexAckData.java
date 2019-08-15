package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * FetchIndexAckData
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class FetchIndexAckData {

    private long index;
    private JoyQueueCode code;

    public FetchIndexAckData() {

    }

    public FetchIndexAckData(long index, JoyQueueCode code) {
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