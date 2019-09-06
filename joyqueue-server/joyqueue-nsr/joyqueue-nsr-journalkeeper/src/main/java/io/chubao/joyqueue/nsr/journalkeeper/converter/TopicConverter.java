package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.domain.TopicDTO;
import io.chubao.joyqueue.nsr.journalkeeper.helper.ArrayHelper;
import org.apache.commons.collections.CollectionUtils;

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
        return topicDTO;
    }

    public static Topic convert(TopicDTO topicDTO) {
        if (topicDTO == null) {
            return null;
        }

        Topic topic = new Topic();
        topic.setName(TopicName.parse(topicDTO.getCode(), topicDTO.getNamespace()));
        topic.setPartitions(topicDTO.getPartitions());
        topic.setType(Topic.Type.TOPIC);
        topic.setPriorityPartitions(ArrayHelper.toShortSet(topicDTO.getPriorityPartitions()));
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