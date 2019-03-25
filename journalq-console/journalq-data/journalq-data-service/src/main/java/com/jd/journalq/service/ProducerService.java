package com.jd.journalq.service;

import com.jd.journalq.model.domain.Producer;
import com.jd.journalq.model.query.QProducer;
import com.jd.journalq.nsr.NsrService;

public interface ProducerService extends NsrService<Producer, QProducer,String> {

    Producer findByTopicAppGroup(String namespace,String topic,String app);

}
