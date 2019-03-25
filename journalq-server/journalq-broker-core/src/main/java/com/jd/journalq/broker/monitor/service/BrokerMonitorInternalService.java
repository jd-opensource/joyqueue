package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.broker.monitor.stat.BrokerStatExt;
import com.jd.journalq.common.monitor.BrokerMonitorInfo;

/**
 * broker monitor service
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface BrokerMonitorInternalService {

    // TODO jvm监控

    /**
     * 获取broker信息
     *
     * @return
     */
    BrokerMonitorInfo getBrokerInfo();

    /**
     * thread safe
     * broker state 扩展信息,扩展信息包含topic>app>partitionGroup>partition积压
     * and broker id
     * @return  BrokerStatExt
     **/
    BrokerStatExt getExtendBrokerStat(long timeStamp);
}