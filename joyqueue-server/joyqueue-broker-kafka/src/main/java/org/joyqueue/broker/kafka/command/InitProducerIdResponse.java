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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

/**
 * InitProducerIdResponse
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class InitProducerIdResponse extends KafkaRequestOrResponse {

    public static final long NO_PRODUCER_ID = -1L;
    public static final short NO_PRODUCER_EPOCH = -1;

    private short code;
    private long producerId;
    private short producerEpoch;

    public InitProducerIdResponse() {

    }

    public InitProducerIdResponse(short code, long producerId, short producerEpoch) {
        this.code = code;
        this.producerId = producerId;
        this.producerEpoch = producerEpoch;
    }

    public InitProducerIdResponse(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public void setCode(short code) {
        this.code = code;
    }

    public long getProducerId() {
        return producerId;
    }

    public void setProducerId(long producerId) {
        this.producerId = producerId;
    }

    public short getProducerEpoch() {
        return producerEpoch;
    }

    public void setProducerEpoch(short producerEpoch) {
        this.producerEpoch = producerEpoch;
    }

    @Override
    public int type() {
        return KafkaCommandType.INIT_PRODUCER_ID.getCode();
    }
}