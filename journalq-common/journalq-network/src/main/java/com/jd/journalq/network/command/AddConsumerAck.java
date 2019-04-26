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
package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * AddConsumerAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddConsumerAck extends JMQPayload {

    private Map<String, String> consumerIds;

    @Override
    public int type() {
        return JournalqCommandType.ADD_CONSUMER_ACK.getCode();
    }

    public void setConsumerIds(Map<String, String> consumerIds) {
        this.consumerIds = consumerIds;
    }

    public Map<String, String> getConsumerIds() {
        return consumerIds;
    }
}