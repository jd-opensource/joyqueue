/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaErrorCode;
import io.chubao.joyqueue.broker.kafka.command.ProduceRequest;
import io.chubao.joyqueue.broker.kafka.command.ProduceResponse;
import io.chubao.joyqueue.broker.kafka.model.ProducePartitionGroupRequest;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/6
 */
public class ProduceHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceHandler.class);

    private Produce produce;

    public ProduceHandler(Produce produce) {
        this.produce = produce;
    }

    public void produceMessage(ProduceRequest request, QosLevel qosLevel, Producer producer,
                               ProducePartitionGroupRequest partitionGroupRequest, EventListener<ProduceResponse.PartitionResponse> listener) {
        try {
            produce.putMessageAsync(producer, partitionGroupRequest.getMessages(), qosLevel, (writeResult) -> {
                if (!writeResult.getCode().equals(JoyQueueCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                short code = KafkaErrorCode.joyQueueCodeFor(writeResult.getCode().getCode());
                listener.onEvent(new ProduceResponse.PartitionResponse(ProduceResponse.PartitionResponse.NONE_OFFSET, code));
            });
        } catch (Exception e) {
            logger.error("produce message failed, topic: {}", producer.getTopic(), e);
            short code = KafkaErrorCode.exceptionFor(e);
            listener.onEvent(new ProduceResponse.PartitionResponse(ProduceResponse.PartitionResponse.NONE_OFFSET, code));
        }
    }
}