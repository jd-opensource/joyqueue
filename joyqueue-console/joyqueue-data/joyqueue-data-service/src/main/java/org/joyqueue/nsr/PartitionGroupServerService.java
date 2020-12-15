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
package org.joyqueue.nsr;

import org.joyqueue.domain.Replica;
import org.joyqueue.model.domain.TopicPartitionGroup;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public interface PartitionGroupServerService extends NsrService<TopicPartitionGroup, String> {

    List<TopicPartitionGroup> findByTopic(String topic,String namespace);

    TopicPartitionGroup findByTopicAndGroup(String namespace, String topic, Integer groupNo);

    List<Replica> getByBrokerId(Integer brokerId);
}
