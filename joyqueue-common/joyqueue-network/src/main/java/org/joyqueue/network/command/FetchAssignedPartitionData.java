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
package org.joyqueue.network.command;

/**
 * FetchAssignedPartitionData
 *
 * author: gaohaoxiang
 * date: 2018/12/12
 */
public class FetchAssignedPartitionData {

    private String topic;
    private int sessionTimeout;
    private boolean nearby;

    public FetchAssignedPartitionData() {

    }

    public FetchAssignedPartitionData(String topic, int sessionTimeout, boolean nearby) {
        this.topic = topic;
        this.sessionTimeout = sessionTimeout;
        this.nearby = nearby;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public boolean isNearby() {
        return nearby;
    }

    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }

    @Override
    public String toString() {
        return "FetchAssignedPartitionData{" +
                "topic='" + topic + '\'' +
                ", sessionTimeout=" + sessionTimeout +
                ", nearby=" + nearby +
                '}';
    }
}