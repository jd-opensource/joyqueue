package io.chubao.joyqueue.nsr.ignite.dao;

import io.chubao.joyqueue.nsr.ignite.model.IgniteBroker;
import io.chubao.joyqueue.nsr.model.BrokerQuery;

public interface BrokerDao extends BaseDao<IgniteBroker, BrokerQuery, Integer> {
    /**
     * 根据IP端口获取
     *
     * @param ip
     * @param port
     * @return
     */
    IgniteBroker getByIpAndPort(String ip, int port);
}
