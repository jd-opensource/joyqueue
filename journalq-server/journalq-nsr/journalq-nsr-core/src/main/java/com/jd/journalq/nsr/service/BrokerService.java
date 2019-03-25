package com.jd.journalq.nsr.service;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.nsr.model.BrokerQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface BrokerService extends DataService<Broker, BrokerQuery, Integer> {

    /**
     * 根据IP和端口获取Broker
     *
     * @param brokerIp
     * @param brokerPort
     * @return
     */
    Broker getByIpAndPort(String brokerIp, Integer brokerPort);

    /**
     * 根据重试类型查询broker
     * @param retryType
     * @return
     */
    List<Broker> getByRetryType(String retryType);

    /**
     * 根据ids 查询所有broker集合
     * @param ids
     * @return
     */
    List<Broker> getByIds(List<Integer> ids);
}
