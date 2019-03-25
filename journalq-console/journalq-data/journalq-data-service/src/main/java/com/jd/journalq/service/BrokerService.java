package com.jd.journalq.service;

import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.query.QBroker;
import com.jd.journalq.nsr.NsrService;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public interface BrokerService extends NsrService<Broker,QBroker,Long> {

//    /**
//     * 生成Broker
//     * @param model
//     * @return
//     */
//    void generateBroker(Hosts hosts, Broker model);
//
//    Broker findByIp(String ip);

    List<Broker> getByIdsBroker(List<Integer> ids) throws Exception;
    /**
     * 同步所有broker
     * @throws Exception
     */
    List<Broker> syncBrokers() throws Exception;

    List<Broker> findByTopic(String topic) throws Exception;

    List<Broker> queryBrokerList(QBroker qBroker) throws Exception;

}
