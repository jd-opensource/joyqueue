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
package org.joyqueue.client.internal.consumer.domain;

import org.joyqueue.toolkit.time.SystemClock;

/**
 * LocalIndexData
 *
 * author: gaohaoxiang
 * date: 2018/12/14
 */
public class LocalIndexData {

    private long index;
    private long updateTime;
    private long createTime;

    public LocalIndexData() {

    }

    public LocalIndexData(long index, long updateTime, long createTime) {
        this.index = index;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }

    public boolean isExpired(long expireTime) {
        return (SystemClock.now() - updateTime > expireTime);
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}