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
package org.joyqueue.broker.coordinator.group.domain;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ExpiredGroupMemberMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class ExpiredGroupMemberMetadata {

    private String host;
    private AtomicInteger expireTimes = new AtomicInteger(0);
    private long latestHeartbeat;
    private long expireTime;

    public ExpiredGroupMemberMetadata() {

    }

    public ExpiredGroupMemberMetadata(String host) {
        this.host = host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setExpireTimes(AtomicInteger expireTimes) {
        this.expireTimes = expireTimes;
    }

    public AtomicInteger getExpireTimes() {
        return expireTimes;
    }

    public long getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(long latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }
}