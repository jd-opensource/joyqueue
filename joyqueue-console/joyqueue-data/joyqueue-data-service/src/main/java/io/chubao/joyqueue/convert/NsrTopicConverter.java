package io.chubao.joyqueue.convert;

import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.Topic;

/**
 * Created by wangxiaofei1 on 2019/1/4.
 */
public class NsrTopicConverter extends Converter<Topic, io.chubao.joyqueue.domain.Topic> {
    @Override
    protected io.chubao.joyqueue.domain.Topic forward(Topic topic) {
        io.chubao.joyqueue.domain.Topic nsrTopic = new io.chubao.joyqueue.domain.Topic();
        nsrTopic.setName(CodeConverter.convertTopic(topic.getNamespace(),new Topic(topic.getId(),topic.getCode())));
        nsrTopic.setType(io.chubao.joyqueue.domain.Topic.Type.valueOf((byte)topic.getType()));
        nsrTopic.setPartitions((short)topic.getPartitions());
        return nsrTopic;
    }

    @Override
    protected Topic backward(io.chubao.joyqueue.domain.Topic nsrTopic) {
        Topic topic = new Topic();
        topic.setId(nsrTopic.getName().getFullName());
        topic.setPartitions(nsrTopic.getPartitions());
        topic.setType(nsrTopic.getType().code());
        topic.setNamespace(new Namespace(nsrTopic.getName().getNamespace()));
        topic.setCode(nsrTopic.getName().getCode());
        return topic;
    }
}
