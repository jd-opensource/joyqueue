package com.jd.journalq.nsr;

import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.query.QBroker;

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
