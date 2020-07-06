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
package org.joyqueue.broker.polling;

import org.joyqueue.network.session.Consumer;
import org.joyqueue.toolkit.time.SystemClock;

/**
 * Created by chengzhiliang on 2018/9/5.
 */
public class LongPolling {
    private Consumer consumer;
    private int count;
    private int ackTimeout;
    private long longPollingTimeout;
    // 长轮询回调
    private LongPollingCallback longPollingCallback;
    // 过期时间
    private long expire;

    public LongPolling(Consumer consumer, int count, int ackTimeout, long longPollingTimeout, LongPollingCallback longPollingCallback) {
        this.consumer = consumer;
        this.count = count;
        this.ackTimeout = ackTimeout;
        this.longPollingTimeout = longPollingTimeout;
        this.longPollingCallback = longPollingCallback;
        this.expire = SystemClock.now() + longPollingTimeout;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public long getLongPollingTimeout() {
        return longPollingTimeout;
    }

    public void setLongPollingTimeout(long longPollingTimeout) {
        this.longPollingTimeout = longPollingTimeout;
    }

    public LongPollingCallback getLongPollingCallback() {
        return longPollingCallback;
    }

    public void setLongPollingCallback(LongPollingCallback longPollingCallback) {
        this.longPollingCallback = longPollingCallback;
    }

    public long getExpire() {
        return expire;
    }

    @Override
    public String toString() {
        return "LongPolling{" +
                "consumer=" + consumer +
                ", count=" + count +
                ", ackTimeout=" + ackTimeout +
                ", longPollingTimeout=" + longPollingTimeout +
                ", longPollingCallback=" + longPollingCallback +
                ", expire=" + expire +
                '}';
    }
}
