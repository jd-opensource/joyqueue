package com.jd.journalq.manage;

import java.io.Serializable;

public class IndexItem implements Serializable {
    public IndexItem() {
    }

    public IndexItem(long offset, int length) {
        this.offset = offset;
        this.length = length;
    }

    private long offset;
    private int length;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}