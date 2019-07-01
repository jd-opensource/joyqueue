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
package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.transport.command.JournalqPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndApp extends JournalqPayload {
    private TopicName topic;
    private String app;
    public GetProducerByTopicAndApp app(String app){
        this.app = app;
        return this;
    }
    public GetProducerByTopicAndApp topic(TopicName topic){
        this.topic = topic;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP;
    }

    @Override
    public String toString() {
        return "GetProducerByTopicAndApp{" +
                "topic=" + topic +
                ", app='" + app + '\'' +
                '}';
    }
}
