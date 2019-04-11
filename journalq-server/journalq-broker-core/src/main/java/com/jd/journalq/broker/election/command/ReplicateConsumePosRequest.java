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
package com.jd.journalq.broker.election.command;

import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ReplicateConsumePosRequest extends JMQPayload {
    private String consumePositions;

    public ReplicateConsumePosRequest(String consumePositions) {
        this.consumePositions = consumePositions;
    }

    public String getConsumePositions() {
        return consumePositions;
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
