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
package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.ProduceResponse;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ProduceHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/6
 */
public class ProduceHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceHandler.class);

    private Produce produce;

    public ProduceHandler(Produce produce) {
        this.produce = produce;
    }

    public void produceMessage(QosLevel qosLevel, Producer producer, List<BrokerMessage> messages, EventListener<ProduceResponse.PartitionResponse> listener) {
        try {
            produce.putMessageAsync(producer, messages, qosLevel, (writeResult) -> {
                if (!writeResult.getCode().equals(JournalqCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                short code = KafkaErrorCode.journalqCodeFor(writeResult.getCode().getCode());
                listener.onEvent(new ProduceResponse.PartitionResponse(0, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
            });
        } catch (Exception e) {
            logger.error("produce message failed, topic: {}", producer.getTopic(), e);
            short code = KafkaErrorCode.exceptionFor(e);
            listener.onEvent(new ProduceResponse.PartitionResponse(0, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
        }
    }
}