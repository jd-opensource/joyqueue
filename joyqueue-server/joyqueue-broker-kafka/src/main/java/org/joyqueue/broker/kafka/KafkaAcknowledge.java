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
package org.joyqueue.broker.kafka;

import org.joyqueue.domain.QosLevel;

/**
 * KafkaAcknowledge
 *
 * author: gaohaoxiang
 * date: 2018/11/6
 */
public enum KafkaAcknowledge {

    /**
     * 不应答
     */
    ACK_NO(0),

    /**
     * 接收到数据应答
     */
    ACK_RECEIVE(1),

    /**
     * 刷盘后应答
     */
    ACK_FLUSH(-1),

    ;

    private int value;

    KafkaAcknowledge(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static KafkaAcknowledge valueOf(short value) {
        switch (value) {
            case 0:
                return ACK_NO;
            case 1:
                return ACK_RECEIVE;
            case -1:
                return ACK_FLUSH;
            default:
                throw new UnsupportedOperationException("unsupported kafkaAcknowledge, value: " + value);
        }
    }

    public static QosLevel convertToQosLevel(KafkaAcknowledge kafkaAcknowledge) {
        switch (kafkaAcknowledge) {
            case ACK_FLUSH:
                return QosLevel.REPLICATION;
            case ACK_RECEIVE:
                return QosLevel.RECEIVE;
            case ACK_NO:
                return QosLevel.ONE_WAY;
            default:
                throw new UnsupportedOperationException("unsupported kafkaAcknowledge to acknowledge, value: " + kafkaAcknowledge);
        }
    }
}