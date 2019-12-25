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
package org.joyqueue.broker.kafka.network;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Header;

/**
 * kafka协议头
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class KafkaHeader implements Header {

    private short apiKey;
    private short apiVersion;
    private int correlationId;
    private String clientId;
    private Direction direction;
    private QosLevel qosLevel;

    public KafkaHeader() {}

    public KafkaHeader(short apiKey, short apiVersion, int correlationId) {
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.correlationId = correlationId;
    }

    public KafkaHeader(short apiKey, short apiVersion, int correlationId, QosLevel qosLevel, Direction direction) {
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.correlationId = correlationId;
        this.direction = direction;
        this.qosLevel = qosLevel;
    }

    public short getApiKey() {
        return apiKey;
    }

    public void setApiKey(short apiKey) {
        this.apiKey = apiKey;
    }

    public short getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(short apiVersion) {
        this.apiVersion = apiVersion;
    }

    public int getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(int correlationId) {
        this.correlationId = correlationId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public int getRequestId() {
        return correlationId;
    }

    @Override
    public void setRequestId(int requestId) {
        this.correlationId = requestId;
    }

    @Override
    public int getType() {
        return (int)apiKey;
    }

    @Override
    public void setType(int type) {
        this.apiKey = (short)type;
    }

    @Override
    public void setVersion(int version) {
        this.apiVersion = (short) version;
    }

    @Override
    public int getVersion() {
        return (int) apiVersion;
    }

    @Override
    public QosLevel getQosLevel() {
        return qosLevel;
    }

    @Override
    public void setQosLevel(QosLevel qosLevel) {
        this.qosLevel = qosLevel;
    }

    @Override
    public void setStatus(int status) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getError() {
        return null;
    }

    @Override
    public void setError(String msg) {

    }

    @Override
    public String toString() {
        return String.format("KafkaHeader: {apiKey: %s. apiVersion: %s, correlationId: %s}", apiKey, apiVersion, correlationId);
    }

    /**
     * 构造器
     */
    public static class Builder {
        KafkaHeader header = new KafkaHeader();

        public static Builder create() {
            return new Builder();
        }

        public static KafkaHeader response() {
            return create().direction(Direction.RESPONSE).build();
        }

        public static KafkaHeader response(int correlationId) {
            return create().direction(Direction.RESPONSE).build();
        }

        public static KafkaHeader request() {
            return create().direction(Direction.REQUEST).build();
        }

        public Builder correlationId(int correlationId) {
            header.setCorrelationId(correlationId);
            return this;
        }

        public Builder direction(Direction direction) {
            header.setDirection(direction);
            return this;
        }

        public Builder qosLevel(QosLevel qosLevel) {
            header.setQosLevel(qosLevel);
            return this;
        }

        public KafkaHeader build() {
            return header;
        }

    }
}