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
package org.joyqueue.broker.kafka.handler;

import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.command.OffsetFetchRequest;
import org.joyqueue.broker.kafka.command.OffsetFetchResponse;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * OffsetFetchRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class OffsetFetchRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(OffsetFetchRequestHandler.class);

    private GroupCoordinator groupCoordinator;
    private KafkaConfig config;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
        this.config = kafkaContext.getConfig();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        OffsetFetchRequest offsetFetchRequest = (OffsetFetchRequest) command.getPayload();
//        String groupId = offsetFetchRequest.getGroupId();
        String groupId = KafkaClientHelper.parseClient(offsetFetchRequest.getClientId());
        Map<String, List<Integer>> topicAndPartitions = offsetFetchRequest.getTopicAndPartitions();
        Map<String, List<OffsetMetadataAndError>> result = groupCoordinator.handleFetchOffsets(groupId, topicAndPartitions);

        if (config.getLogDetail(offsetFetchRequest.getClientId())) {
            logger.info("fetch offset, transport: {}, app: {}, request: {}, response: {}",
                    transport, offsetFetchRequest.getClientId(), offsetFetchRequest, result);
        }

        OffsetFetchResponse offsetFetchResponse = new OffsetFetchResponse(result);
        return new Command(offsetFetchResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }
}
