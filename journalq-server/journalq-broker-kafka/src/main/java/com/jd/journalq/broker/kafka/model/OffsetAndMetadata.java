/**
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
package com.jd.journalq.broker.kafka.model;

/**
 * OffsetAndMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class OffsetAndMetadata {

    public static final long INVALID_OFFSET = -1L;
    public static final String NO_METADATA = "";

    private long offset;
    private String metadata;
    private long offsetCommitTime;

    public OffsetAndMetadata() {
    }

    public OffsetAndMetadata(long offset, String metadata) {
        this.offset = offset;
        this.metadata = metadata;
    }

    public OffsetAndMetadata(long offset, String metadata, long offsetCommitTime) {
        this.offset = offset;
        this.metadata = metadata;
        this.offsetCommitTime = offsetCommitTime;
    }

    public long getOffset() {
        return offset;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    public long getOffsetCommitTime() {
        return offsetCommitTime;
    }

    public void setOffsetCommitTime(long offsetCommitTime) {
        this.offsetCommitTime = offsetCommitTime;
    }

    @Override
    public String toString() {
        return "OffsetAndMetadata{" +
                "offset=" + offset +
                ", metadata='" + metadata + '\'' +
                ", offsetCommitTime=" + offsetCommitTime +
                '}';
    }
}

