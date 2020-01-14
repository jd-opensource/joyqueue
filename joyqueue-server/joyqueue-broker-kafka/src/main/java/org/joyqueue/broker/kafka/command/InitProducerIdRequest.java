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
 * InitProducerIdRequest
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class InitProducerIdRequest extends KafkaRequestOrResponse {

    private String transactionId;
    private int transactionTimeout;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    @Override
    public int type() {
        return KafkaCommandType.INIT_PRODUCER_ID.getCode();
    }

    @Override
    public String toString() {
        return "InitProducerIdRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionTimeout=" + transactionTimeout +
                '}';
    }
}