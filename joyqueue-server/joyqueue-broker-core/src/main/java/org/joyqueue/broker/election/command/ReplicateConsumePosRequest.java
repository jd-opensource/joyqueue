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
package org.joyqueue.broker.election.command;

import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ReplicateConsumePosRequest extends JoyQueuePayload {
    private Map<ConsumePartition, Position> consumePositions;

    public Map<ConsumePartition, Position> getConsumePositions() {
        return consumePositions;
    }

    public void setConsumePositions(Map<ConsumePartition, Position> consumePositions) {
        this.consumePositions = consumePositions;
    }

    public ReplicateConsumePosRequest(Map<ConsumePartition, Position> consumePositions) {
        this.consumePositions = consumePositions;
    }

    public ReplicateConsumePosRequest() {
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
