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
package org.joyqueue.broker.monitor.stat;

import com.google.common.collect.Maps;
import org.joyqueue.broker.monitor.metrics.Metrics;
import org.joyqueue.monitor.Client;

import java.util.concurrent.ConcurrentMap;

/**
 * connectionStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class ConnectionStat {

    private Metrics consumer = new Metrics();
    private Metrics producer = new Metrics();
    private ConcurrentMap<String /** connectionId **/, Client> connectionMap = Maps.newConcurrentMap();

    public void incrConsumer() {
        this.incrConsumer(1);
    }

    public void incrConsumer(int count) {
        this.consumer.mark(count);
    }

    public void decrConsumer() {
        this.decrConsumer(1);
    }

    public void decrConsumer(int count) {
        this.consumer.mark(-count);
    }

    public void incrProducer() {
        this.incrProducer(1);
    }

    public void incrProducer(int count) {
        this.producer.mark(count);
    }

    public void decrProducer() {
        this.decrProducer(1);
    }

    public void decrProducer(int count) {
        this.producer.mark(-count);
    }

    public int getConsumer() {
        return (int) this.consumer.getCount();
    }

    public int getProducer() {
        return (int) this.producer.getCount();
    }

    public int getConnection() {
        return this.connectionMap.size();
    }

    public Client getConnection(String connectionId) {
        return connectionMap.get(connectionId);
    }

    public boolean addConnection(Client client) {
        return connectionMap.putIfAbsent(client.getConnectionId(), client) == null;
    }

    public boolean removeConnection(String connectionId) {
        return connectionMap.remove(connectionId) != null;
    }

    public ConcurrentMap<String, Client> getConnectionMap() {
        return connectionMap;
    }
}