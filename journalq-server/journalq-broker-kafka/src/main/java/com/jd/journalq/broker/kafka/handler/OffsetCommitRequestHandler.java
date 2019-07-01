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


import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.OffsetCommitRequest;
import com.jd.journalq.broker.kafka.command.OffsetCommitResponse;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * OffsetCommitRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class OffsetCommitRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(OffsetCommitRequestHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        OffsetCommitRequest offsetCommitRequest = (OffsetCommitRequest) command.getPayload();

        Map<String, List<OffsetMetadataAndError>> result = groupCoordinator.handleCommitOffsets(offsetCommitRequest.getGroupId(), offsetCommitRequest.getMemberId(),
                offsetCommitRequest.getGroupGenerationId(), offsetCommitRequest.getOffsets());

        // TODO 临时日志
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, List<OffsetMetadataAndError>> entry : result.entrySet()) {
                for (OffsetMetadataAndError offset : entry.getValue()) {
                    if (offset.getError() == KafkaErrorCode.NONE.getCode()) {
                        logger.debug("offset commit request with correlation id {} from client {} on topic {} partition {} offset {}",
                                offsetCommitRequest.getCorrelationId(), offsetCommitRequest.getGroupId(), entry.getKey(), offset.getPartition(), offset.getOffset());
                    } else {
                        logger.debug("offset commit request with correlation id {} from client {} on topic {} partition {} failed due to {}",
                                offsetCommitRequest.getCorrelationId(), offsetCommitRequest.getGroupId(), entry.getKey(), offset.getPartition(), offset.getOffset());
                    }
                }

            }
        }

        OffsetCommitResponse offsetCommitResponse = new OffsetCommitResponse(result);
        return new Command(offsetCommitResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}