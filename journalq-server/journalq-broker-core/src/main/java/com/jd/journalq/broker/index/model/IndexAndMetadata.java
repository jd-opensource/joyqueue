package com.jd.journalq.broker.index.model;

import com.jd.journalq.toolkit.time.SystemClock;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class IndexAndMetadata {

    public static final long INVALID_INDEX = -1L;
    public static final String NO_METADATA = "";

    private long index;
    private String metadata;

    private long indexCacheRetainTime;
    private long indexCommitTime;

    public IndexAndMetadata() {}

    public IndexAndMetadata(long index, String metadata) {
        this.index = index;
        this.metadata = metadata;

        indexCommitTime = SystemClock.now();
    }

    public long getIndexCacheRetainTime() {
        return indexCacheRetainTime;
    }

    public void setIndexCacheRetainTime(long indexCacheRetainTime) {
        this.indexCacheRetainTime = indexCacheRetainTime;
    }

    public long getIndexCommitTime() {
        return indexCommitTime;
    }

    public void setIndexCommitTime(long indexCommitTime) {
        this.indexCommitTime = indexCommitTime;
    }

    public long getIndex() {
        return index;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}

