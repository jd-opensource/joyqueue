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
package org.joyqueue.client.internal.consumer.converter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TopicMetadataConverter
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class TopicMetadataConverter {

    public static Map<String, TopicMetadata> convertToMap(List<TopicMetadata> topicMetadatas) {
        if (CollectionUtils.isEmpty(topicMetadatas)) {
            return Collections.emptyMap();
        }
        Map<String, TopicMetadata> result = Maps.newHashMap();
        for (TopicMetadata topicMetadata : topicMetadatas) {
            result.put(topicMetadata.getTopic(), topicMetadata);
        }
        return result;
    }

    public static List<String> convertToCodeList(List<TopicMetadata> topicMetadatas) {
        if (CollectionUtils.isEmpty(topicMetadatas)) {
            return Collections.emptyList();
        }
        List<String> topics = Lists.newLinkedList();
        for (TopicMetadata topicMetadata : topicMetadatas) {
            topics.add(topicMetadata.getTopic());
        }
        return topics;
    }
}