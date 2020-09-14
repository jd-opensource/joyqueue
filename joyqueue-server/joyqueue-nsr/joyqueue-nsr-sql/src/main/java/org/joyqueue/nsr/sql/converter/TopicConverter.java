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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.domain.TopicDTO;
import org.joyqueue.nsr.sql.helper.ArrayHelper;
import org.joyqueue.nsr.sql.helper.JsonHelper;

import java.util.Collections;
import java.util.List;

/**
 * TopicConverter
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class TopicConverter {

    public static TopicDTO convert(Topic topic) {
        if (topic == null) {
            return null;
        }

        TopicDTO topicDTO = new TopicDTO();
        topicDTO.setId(topic.getName().getFullName());
        topicDTO.setCode(topic.getName().getCode());
        topicDTO.setNamespace(topic.getName().getNamespace());
        topicDTO.setPartitions(topic.getPartitions());
        topicDTO.setType(topic.getType().code());
        topicDTO.setPriorityPartitions(ArrayHelper.toString(topic.getPriorityPartitions()));
        topicDTO.setPolicy(JsonHelper.toJson(topic.getPolicy()));
        return topicDTO;
    }

    public static Topic convert(TopicDTO topicDTO) {
        if (topicDTO == null) {
            return null;
        }

        Topic topic = new Topic();
        topic.setName(TopicName.parse(topicDTO.getCode(), topicDTO.getNamespace()));
        topic.setPartitions(topicDTO.getPartitions());
        topic.setType(Topic.Type.valueOf(topicDTO.getType()));
        topic.setPriorityPartitions(ArrayHelper.toShortSet(topicDTO.getPriorityPartitions()));
        topic.setPolicy(JsonHelper.parseJson(Topic.TopicPolicy.class, topicDTO.getPolicy()));
        return topic;
    }

    public static List<Topic> convert(List<TopicDTO> topicDTOList) {
        if (CollectionUtils.isEmpty(topicDTOList)) {
            return Collections.emptyList();
        }
        List<Topic> result = Lists.newArrayListWithCapacity(topicDTOList.size());
        for (TopicDTO topicDTO : topicDTOList) {
            result.add(convert(topicDTO));
        }
        return result;
    }
}