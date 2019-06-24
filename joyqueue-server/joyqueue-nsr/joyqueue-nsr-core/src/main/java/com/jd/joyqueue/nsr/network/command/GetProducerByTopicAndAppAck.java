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
package com.jd.joyqueue.nsr.network.command;

import com.jd.joyqueue.domain.Producer;
import com.jd.joyqueue.network.transport.command.JournalqPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndAppAck extends JournalqPayload {
    private Producer producer;
    public GetProducerByTopicAndAppAck producer(Producer producer){
        this.producer = producer;
        return this;
    }

    public Producer getProducer() {
        return producer;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP_ACK;
    }
}
