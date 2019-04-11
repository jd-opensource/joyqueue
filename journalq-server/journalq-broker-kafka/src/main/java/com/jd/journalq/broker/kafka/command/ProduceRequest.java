/**
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
package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.domain.TopicName;

import java.util.List;

/**
 * Created by zhangkepeng on 16-7-27.
 */
public class ProduceRequest extends KafkaRequestOrResponse {
    private short requiredAcks;
    private int ackTimeoutMs;
    private String transactionalId;

    private Table<TopicName, Integer, List<KafkaBrokerMessage>> topicPartitionMessages;

    public short getRequiredAcks() {
        return requiredAcks;
    }

    public void setRequiredAcks(short requiredAcks) {
        this.requiredAcks = requiredAcks;
    }

    public void setAckTimeoutMs(int ackTimeoutMs) {
        this.ackTimeoutMs = ackTimeoutMs;
    }

    public int getAckTimeoutMs() {
        return ackTimeoutMs;
    }

    public String getTransactionalId() {
        return transactionalId;
    }

    public void setTransactionalId(String transactionalId) {
        this.transactionalId = transactionalId;
    }

    public Table<TopicName, Integer, List<KafkaBrokerMessage>> getTopicPartitionMessages() {
        return topicPartitionMessages;
    }

    public void setTopicPartitionMessages(Table<TopicName, Integer, List<KafkaBrokerMessage>> topicPartitionMessages) {
        this.topicPartitionMessages = topicPartitionMessages;
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    private String describe() {
        StringBuilder producerRequest = new StringBuilder();
        producerRequest.append("Name: " + this.getClass().getSimpleName());
        producerRequest.append("; RequiredAcks: " + requiredAcks);
        producerRequest.append("; AckTimeoutMs: " + ackTimeoutMs + " ms");
        return producerRequest.toString();
    }
}