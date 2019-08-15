package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.query.QProducer;
import io.chubao.joyqueue.nsr.model.ProducerQuery;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface ProducerNameServerService extends NsrService<Producer,QProducer,String> {

    List<Producer> syncProducer(byte clientType) throws Exception;

    List<Producer> getListProducer(ProducerQuery producerQuery) throws Exception;

    Producer findByTopicAppGroup(String namespace,  String topic, String app);

}
