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
package org.joyqueue.convert;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.TopicPartitionGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Partition group converter
 *  Created by chenyanying3 on 2018-11-29.
 */
public class PartitionGroupConverter {
    public static TopicPartitionGroup convert(PartitionGroup partitionGroup) {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup();
        topicPartitionGroup.setGroupNo(partitionGroup.getGroup());
        topicPartitionGroup.setPartitions(partitionGroup.getPartitions().toString());
        topicPartitionGroup.setElectType(partitionGroup.getElectType().type());
        topicPartitionGroup.setTopic(new Topic(partitionGroup.getTopic().getCode()));
        topicPartitionGroup.setNamespace(new Namespace(partitionGroup.getTopic().getNamespace()));
        topicPartitionGroup.setIsr(partitionGroup.getIsrs());
        topicPartitionGroup.setLeader(partitionGroup.getLeader());
        topicPartitionGroup.setLearners(partitionGroup.getLearners());
        topicPartitionGroup.setTerm(partitionGroup.getTerm());
        return topicPartitionGroup;
    }

    public static List<TopicPartitionGroup> convert(List<PartitionGroup> partitionGroups) {
        if (partitionGroups == null || partitionGroups.isEmpty()) {
            return Collections.emptyList();
        }
        List<TopicPartitionGroup> resultList = new ArrayList<>();
        partitionGroups.forEach(partitionGroup -> resultList.add(PartitionGroupConverter.convert(partitionGroup)));
        return resultList;
    }
}
