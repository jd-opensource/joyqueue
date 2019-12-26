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
package org.joyqueue.client.internal.consumer.coordinator.domain;

import org.joyqueue.toolkit.time.SystemClock;

/**
 * BrokerAssignmentsHolder
 *
 * author: gaohaoxiang
 * date: 2018/12/11
 */
public class BrokerAssignmentsHolder {

    private BrokerAssignments brokerAssignments;
    private long createTime;

    public BrokerAssignmentsHolder(BrokerAssignments brokerAssignments, long createTime) {
        this.brokerAssignments = brokerAssignments;
        this.createTime = createTime;
    }

    public boolean isExpired(long expireTime) {
        return (createTime + expireTime < SystemClock.now());
    }

    public BrokerAssignments getBrokerAssignments() {
        return brokerAssignments;
    }

    public void setBrokerAssignments(BrokerAssignments brokerAssignments) {
        this.brokerAssignments = brokerAssignments;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}