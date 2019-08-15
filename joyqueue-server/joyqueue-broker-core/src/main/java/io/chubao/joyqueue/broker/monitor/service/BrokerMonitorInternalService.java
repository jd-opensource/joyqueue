package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.broker.monitor.stat.BrokerStatExt;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;

/**
 * broker monitor service
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface BrokerMonitorInternalService {

    /**
     * 获取监控信息
     *
     * @return broker信息
     */
    BrokerMonitorInfo getBrokerInfo();

    /**
     * 获取扩展监控信息，包括额外的积压信息等
     *
     * @param timeStamp 时间戳，会写回到返回值
     * @return broker扩展信息
     */
    BrokerStatExt getExtendBrokerStat(long timeStamp);

    /**
     * 获取启动信息
     *
     * @return
     */
    BrokerStartupInfo getStartInfo();
}