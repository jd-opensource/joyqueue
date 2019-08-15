package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.query.QProducer;
import io.chubao.joyqueue.nsr.NsrService;

public interface ProducerService extends NsrService<Producer, QProducer,String> {

    Producer findByTopicAppGroup(String namespace,String topic,String app);

}
