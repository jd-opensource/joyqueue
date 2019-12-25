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
package org.joyqueue.monitor;

/**
 * @author majun8
 */
public class MqttSummaryInfo extends BaseMonitorInfo {

    private long totalConnections;
    private long totalSessions;
    private long totalPublished;
    private long totalConsumed;
    private long totalAcknowledged;
    private long totalRecommit;
    private int consumePool;
    private int deliveryPool;

    public long getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(long totalConnections) {
        this.totalConnections = totalConnections;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getTotalPublished() {
        return totalPublished;
    }

    public void setTotalPublished(long totalPublished) {
        this.totalPublished = totalPublished;
    }

    public long getTotalConsumed() {
        return totalConsumed;
    }

    public void setTotalConsumed(long totalConsumed) {
        this.totalConsumed = totalConsumed;
    }

    public long getTotalAcknowledged() {
        return totalAcknowledged;
    }

    public void setTotalAcknowledged(long totalAcknowledged) {
        this.totalAcknowledged = totalAcknowledged;
    }

    public long getTotalRecommit() {
        return totalRecommit;
    }

    public void setTotalRecommit(long totalRecommit) {
        this.totalRecommit = totalRecommit;
    }

    public int getConsumePool() {
        return consumePool;
    }

    public void setConsumePool(int consumePool) {
        this.consumePool = consumePool;
    }

    public int getDeliveryPool() {
        return deliveryPool;
    }

    public void setDeliveryPool(int deliveryPool) {
        this.deliveryPool = deliveryPool;
    }
}
