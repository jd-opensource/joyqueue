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
package org.joyqueue.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * 订阅
 */
public class Subscription implements Serializable {
    /**
     * 主题
     */

    protected TopicName topic;
    /**
     * 应用OR应用分组
     */
    protected String app;
    /**
     * 类型
     **/
    protected Type type;

    public Subscription() {
    }

    public Subscription(TopicName topic, String app, Type type) {
        this.topic = topic;
        this.app = app;
        this.type = type;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        //发送
        PRODUCTION((byte) 1),
        //消费
        CONSUMPTION((byte) 2);

        private byte value;

        Type(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static Type valueOf(byte value) {
            for (Type type : Type.values()) {
                if (value == type.value) {
                    return type;
                }
            }

            throw new IllegalArgumentException("type do not supported");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || !(o instanceof Subscription)){
            return false;
        }
        Subscription that = (Subscription) o;
        return Objects.equals(topic, that.topic) &&
                Objects.equals(app, that.app) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, app, type);
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "topic=" + topic +
                ", app='" + app + '\'' +
                ", type=" + type +
                '}';
    }
}
