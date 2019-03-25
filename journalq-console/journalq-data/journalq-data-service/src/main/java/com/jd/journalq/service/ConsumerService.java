package com.jd.journalq.service;

import com.jd.journalq.model.domain.Consumer;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.nsr.NsrService;

import java.util.List;


public interface ConsumerService extends NsrService<Consumer, QConsumer,String> {

    Consumer findByTopicAppGroup(String namespace,String topic,String app,String group);

    List<String> findAllSubscribeGroups();

}
