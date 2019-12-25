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
package org.joyqueue.network.command;

import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * AddConsumerResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddConsumerResponse extends JoyQueuePayload {

    private Map<String, String> consumerIds;

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONSUMER_RESPONSE.getCode();
    }

    public void setConsumerIds(Map<String, String> consumerIds) {
        this.consumerIds = consumerIds;
    }

    public Map<String, String> getConsumerIds() {
        return consumerIds;
    }
}