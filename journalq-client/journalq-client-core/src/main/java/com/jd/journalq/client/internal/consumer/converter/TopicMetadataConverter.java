package com.jd.journalq.client.internal.consumer.converter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TopicMetadataConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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