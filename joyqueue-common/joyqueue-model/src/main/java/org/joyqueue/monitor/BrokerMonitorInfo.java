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
 * broker信息
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class BrokerMonitorInfo extends BaseMonitorInfo {

    private ConnectionMonitorInfo connection;
    private DeQueueMonitorInfo deQueue;
    private EnQueueMonitorInfo enQueue;
    private ReplicationMonitorInfo replication;
    private StoreMonitorInfo store;
    private NameServerMonitorInfo nameServer;
    private ElectionMonitorInfo election;
    private BufferPoolMonitorInfo bufferPoolMonitorInfo;
    private BrokerStartupInfo startupInfo;

    public ConnectionMonitorInfo getConnection() {
        return connection;
    }

    public void setConnection(ConnectionMonitorInfo connection) {
        this.connection = connection;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public EnQueueMonitorInfo getEnQueue() {
        return enQueue;
    }

    public void setEnQueue(EnQueueMonitorInfo enQueue) {
        this.enQueue = enQueue;
    }

    public void setReplication(ReplicationMonitorInfo replication) {
        this.replication = replication;
    }

    public ReplicationMonitorInfo getReplication() {
        return replication;
    }

    public void setStore(StoreMonitorInfo store) {
        this.store = store;
    }

    public StoreMonitorInfo getStore() {
        return store;
    }

    public void setNameServer(NameServerMonitorInfo nameServer) {
        this.nameServer = nameServer;
    }

    public NameServerMonitorInfo getNameServer() {
        return nameServer;
    }

    public void setElection(ElectionMonitorInfo election) {
        this.election = election;
    }

    public ElectionMonitorInfo getElection() {
        return election;
    }

    public BufferPoolMonitorInfo getBufferPoolMonitorInfo() {
        return bufferPoolMonitorInfo;
    }

    public void setBufferPoolMonitorInfo(BufferPoolMonitorInfo bufferPoolMonitorInfo) {
        this.bufferPoolMonitorInfo = bufferPoolMonitorInfo;
    }

    public BrokerStartupInfo getStartupInfo() {
        return startupInfo;
    }

    public void setStartupInfo(BrokerStartupInfo startupInfo) {
        this.startupInfo = startupInfo;
    }
}