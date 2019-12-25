/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.index.model;

import org.joyqueue.toolkit.time.SystemClock;

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

    @Override
    public String toString() {
        return "IndexAndMetadata{" +
                "index=" + index +
                ", metadata='" + metadata + '\'' +
                ", indexCacheRetainTime=" + indexCacheRetainTime +
                ", indexCommitTime=" + indexCommitTime +
                '}';
    }
}

