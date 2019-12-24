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
package org.joyqueue.nsr.ignite.model;

import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.domain.TopicType;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;


/**
 * 订阅
 *
 * @author lixiaobin6
 * 下午3:23 2018/7/31
 */
public class IgniteConsumer extends Consumer implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_APP = "app";
    public static final String COLUMN_CLIENT_TYPE = "client_type";
    public static final String COLUMN_TOPIC_TYPE = "topic_type";
    /**
     * 该字段是app.group中的app
     */
    public static final String COLUMN_REFER = "referer";

    /**
     * id:namespace+topic+app
     */
    private String id;

    public IgniteConsumer() {
    }

    public IgniteConsumer(Consumer consumer) {
        this.app = consumer.getApp();
        this.topic = consumer.getTopic();
        this.clientType = consumer.getClientType();
        this.topicType = consumer.getTopicType();
        this.consumerPolicy = consumer.getConsumerPolicy();
        this.retryPolicy = consumer.getRetryPolicy();
        this.limitPolicy = consumer.getLimitPolicy();
    }

    @Override
    public String getId() {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(app).toString();
    }

    public static String getId(TopicName topic, String app) {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(app).toString();
    }

    public IgniteConsumer fillConfig(IgniteConsumerConfig consumerConfig) {
        if (null != consumerConfig) {
            this.consumerPolicy = consumerConfig.getConsumerPolicy();
            this.retryPolicy = consumerConfig.getRetryPolicy();
            this.limitPolicy = consumerConfig.getLimitPolicy();
        }
        return this;
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_TOPIC, topic.getCode());
        writer.writeString(COLUMN_NAMESPACE, topic.getNamespace());
        writer.writeString(COLUMN_APP, app);
        writer.writeByte(COLUMN_CLIENT_TYPE, clientType.value());
        writer.writeByte(COLUMN_TOPIC_TYPE, topicType.code());
        writer.writeString(COLUMN_REFER, app.split(SEPARATOR_SPLIT)[0]);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.id = reader.readString(COLUMN_ID);
        this.topic = new TopicName(reader.readString(COLUMN_TOPIC), reader.readString(COLUMN_NAMESPACE));
        this.app = reader.readString(COLUMN_APP);
        this.clientType = ClientType.valueOf(reader.readByte(COLUMN_CLIENT_TYPE));
        this.type = Type.CONSUMPTION;
        this.topicType = TopicType.valueOf(reader.readByte(COLUMN_TOPIC_TYPE));
    }
}
