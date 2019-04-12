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
package com.jd.journalq.convert;

import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.Topic;

/**
 * Created by wangxiaofei1 on 2019/1/4.
 */
public class NsrTopicConverter extends Converter<Topic, com.jd.journalq.domain.Topic> {
    @Override
    protected com.jd.journalq.domain.Topic forward(Topic topic) {
        com.jd.journalq.domain.Topic nsrTopic = new com.jd.journalq.domain.Topic();
        nsrTopic.setName(CodeConverter.convertTopic(topic.getNamespace(),new Topic(topic.getId(),topic.getCode())));
        nsrTopic.setType(com.jd.journalq.domain.Topic.Type.valueOf((byte)topic.getType()));
        nsrTopic.setPartitions((short)topic.getPartitions());
        return nsrTopic;
    }

    @Override
    protected Topic backward(com.jd.journalq.domain.Topic nsrTopic) {
        Topic topic = new Topic();
        topic.setId(nsrTopic.getName().getFullName());
        topic.setPartitions(nsrTopic.getPartitions());
        topic.setType(nsrTopic.getType().code());
        topic.setNamespace(new Namespace(nsrTopic.getName().getNamespace()));
        topic.setCode(nsrTopic.getName().getCode());
        return topic;
    }
}
