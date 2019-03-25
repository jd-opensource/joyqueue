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
