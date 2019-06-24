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
package com.jd.joyqueue.model.query;

import com.jd.joyqueue.model.QKeyword;
import com.jd.joyqueue.model.domain.Namespace;
import com.jd.joyqueue.model.domain.Topic;

public class QTopicPartitionGroup extends QKeyword {
    private Topic topic;
    private Namespace namespace;
    private Integer group;

    public QTopicPartitionGroup(){}
    public QTopicPartitionGroup(Topic topic){
        this.topic=topic;
    }

    public QTopicPartitionGroup(Topic topic, Namespace namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

    public QTopicPartitionGroup(Topic topic, Namespace namespace, Integer group) {
        this.topic = topic;
        this.namespace = namespace;
        this.group = group;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }
}
