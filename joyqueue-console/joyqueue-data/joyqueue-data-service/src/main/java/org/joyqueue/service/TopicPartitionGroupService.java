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
package org.joyqueue.service;

import org.joyqueue.model.domain.Namespace;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.nsr.NsrService;

import java.util.List;

/**
 * Topic partition group service
 * Created by chenyanying3 on 2018-10-18
 */
public interface TopicPartitionGroupService extends NsrService<TopicPartitionGroup, String> {

   TopicPartitionGroup findByTopicAndGroup(String namespace, String topic,  Integer groupNo);

   List<TopicPartitionGroup> findByTopic(Namespace namespace, Topic topic);
   List<TopicPartitionGroup> findByTopic(String namespace, String topic);
   int removePartition(TopicPartitionGroup model) throws Exception;
   int addPartition(TopicPartitionGroup model) throws Exception;

   int leaderChange(TopicPartitionGroup model) throws Exception;

}
