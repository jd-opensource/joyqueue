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
package org.joyqueue.model.domain;

import org.joyqueue.monitor.DeQueueMonitorInfo;
import org.joyqueue.monitor.EnQueueMonitorInfo;
import org.joyqueue.monitor.PendingMonitorInfo;
import org.joyqueue.monitor.RetryMonitorInfo;


/**
 * monitor info
 *
 **/
public class BrokerMonitorRecord {
    private String app;
    private String topic;
    private String ip;
    private int  partitionGroup;
    private int  partition;
    private long connections;
    private PendingMonitorInfo pending;
    private DeQueueMonitorInfo deQuence;
    private EnQueueMonitorInfo enQuence;
    private RetryMonitorInfo retry;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }

    public PendingMonitorInfo getPending() {
        return pending;
    }

    public void setPending(PendingMonitorInfo pending) {
        this.pending = pending;
    }

    public DeQueueMonitorInfo getDeQuence() {
        return deQuence;
    }

    public void setDeQuence(DeQueueMonitorInfo deQuence) {
        this.deQuence = deQuence;
    }

    public EnQueueMonitorInfo getEnQuence() {
        return enQuence;
    }

    public void setEnQuence(EnQueueMonitorInfo enQuence) {
        this.enQuence = enQuence;
    }

    public RetryMonitorInfo getRetry() {
        return retry;
    }

    public void setRetry(RetryMonitorInfo retry) {
        this.retry = retry;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }
}
