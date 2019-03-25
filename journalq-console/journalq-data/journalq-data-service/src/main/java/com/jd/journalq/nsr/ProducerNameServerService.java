package com.jd.journalq.nsr;

import com.jd.journalq.model.domain.Producer;
import com.jd.journalq.model.query.QProducer;
import com.jd.journalq.nsr.model.ProducerQuery;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface ProducerNameServerService extends NsrService<Producer,QProducer,String> {

    List<Producer> syncProducer(byte clientType) throws Exception;

    List<Producer> getListProducer(ProducerQuery producerQuery) throws Exception;

    Producer findByTopicAppGroup(String namespace,  String topic, String app);

}
