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

import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Types;

import com.google.common.base.Objects;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
public class GetTopics implements Payload, Types {

    private String app;
    /**
     * 0:all
     * 1:producer
     * 2:consumer
     */
    private int subscribeType = 1;
    @Override
    public int[] types() {
        return new int[]{CommandType.GET_TOPICS, JoyQueueCommandType.MQTT_GET_TOPICS.getCode()};
    }

    public String getApp() {
        return app;
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public GetTopics app(String app) {
        this.app = app;
        return this;
    }
    public GetTopics subscribeType(int subscribeType) {
        this.subscribeType = subscribeType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetTopics getTopics = (GetTopics) o;
        return Objects.equal(app, getTopics.app) &&
                Objects.equal(subscribeType, getTopics.subscribeType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(app, subscribeType);
    }

    @Override
    public String toString() {
        return "Subscribe{" +
                "app='" + app + '\'' +
                ", topic='" + subscribeType + '\'' +
                '}';
    }
}
