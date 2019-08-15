package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.query.QBroker;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface BrokerNameServerService extends NsrService<Broker,QBroker,Long> {

    List<Broker> getByIdsBroker(List<Integer> ids) throws Exception;
    /**
     * 同步所有broker
     * @throws Exception
     */
    List<Broker> syncBrokers() throws Exception;
}
