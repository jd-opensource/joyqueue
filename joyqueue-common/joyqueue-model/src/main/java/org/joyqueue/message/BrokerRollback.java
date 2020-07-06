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
package org.joyqueue.message;

import java.util.Map;

/**
 *
 * @author lining11
 * Date: 2018/8/17
 */
public class BrokerRollback implements JoyQueueLog {
    private int size;
    private long startTime;
    private int storeTime;
    private String topic;
    private String app;
    private String txId;
    private Map<Object,Object> attrs;

    @Override
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int getStoreTime() {
        return storeTime;
    }

    @Override
    public void setStoreTime(int storeTime) {
        this.storeTime = storeTime;
    }

    @Override
    public byte getType() {
        return TYPE_TX_ROLLBACK;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Map<Object, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<Object, Object> attrs) {
        this.attrs = attrs;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    @Override
    public String toString() {
        return "BrokerRollback{" +
                "size=" + size +
                ", startTime=" + startTime +
                ", storeTime=" + storeTime +
                ", topic='" + topic + '\'' +
                ", txId='" + txId + '\'' +
                ", attrs=" + attrs +
                '}';
    }
}
