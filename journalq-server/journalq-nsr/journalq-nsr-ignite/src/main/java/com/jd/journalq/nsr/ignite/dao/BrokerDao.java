package com.jd.journalq.nsr.ignite.dao;

import com.jd.journalq.nsr.ignite.model.IgniteBroker;
import com.jd.journalq.nsr.model.BrokerQuery;

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
