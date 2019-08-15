package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.query.QBroker;
import io.chubao.joyqueue.nsr.NsrService;

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
