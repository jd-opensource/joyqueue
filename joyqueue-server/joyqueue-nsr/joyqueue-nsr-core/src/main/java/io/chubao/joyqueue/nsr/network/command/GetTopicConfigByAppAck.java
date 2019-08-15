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
package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByAppAck extends JoyQueuePayload {
    private Map<TopicName, TopicConfig> topicConfigs;

    public GetTopicConfigByAppAck topicConfigs(Map<TopicName, TopicConfig> topicConfigs){
        this.topicConfigs = topicConfigs;
        return this;
    }

    public Map<TopicName, TopicConfig> getTopicConfigs() {
        return topicConfigs;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_APP_ACK;
    }
}
