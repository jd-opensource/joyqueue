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
package com.jd.joyqueue.network.command;

import com.jd.joyqueue.domain.ConsumerPolicy;
import com.jd.joyqueue.domain.ProducerPolicy;
import com.jd.joyqueue.domain.TopicType;
import com.jd.joyqueue.exception.JournalqCode;

import java.io.Serializable;
import java.util.Map;

/**
 * Topic
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class Topic implements Serializable {

    private String topic;
    private ProducerPolicy producerPolicy;
    private ConsumerPolicy consumerPolicy;
    private TopicType type;
    private Map<Integer, TopicPartitionGroup> partitionGroups;
    private JournalqCode code;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public void setProducerPolicy(ProducerPolicy producerPolicy) {
        this.producerPolicy = producerPolicy;
    }

    public ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public void setConsumerPolicy(ConsumerPolicy consumerPolicy) {
        this.consumerPolicy = consumerPolicy;
    }

    public TopicType getType() {
        return type;
    }

    public void setType(TopicType type) {
        this.type = type;
    }

    public Map<Integer, TopicPartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(Map<Integer, TopicPartitionGroup> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    public void setCode(JournalqCode code) {
        this.code = code;
    }

    public JournalqCode getCode() {
        return code;
    }
}