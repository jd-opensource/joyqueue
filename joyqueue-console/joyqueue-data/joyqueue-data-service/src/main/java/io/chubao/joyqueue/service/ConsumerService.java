package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Consumer;
import io.chubao.joyqueue.model.query.QConsumer;
import io.chubao.joyqueue.nsr.NsrService;

import java.util.List;


public interface ConsumerService extends NsrService<Consumer, QConsumer,String> {

    Consumer findByTopicAppGroup(String namespace,String topic,String app,String group);

    List<String> findAllSubscribeGroups();

}
