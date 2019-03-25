package com.jd.journalq.client.internal.consumer.support;

import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.consumer.BrokerLoadBalance;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.client.internal.Plugins;

import java.util.concurrent.ConcurrentMap;

/**
 * BrokerLoadBalanceManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class BrokerLoadBalanceManager {

    private ConcurrentMap<String, BrokerLoadBalance> brokerLoadBalanceMap = Maps.newConcurrentMap();

    public BrokerLoadBalance getBrokerLoadBalance(String topic, String loadBalanceType) {
        BrokerLoadBalance brokerLoadBalance = brokerLoadBalanceMap.get(topic);
        if (brokerLoadBalance == null) {
            brokerLoadBalance = create(loadBalanceType);
            BrokerLoadBalance oldBrokerLoadBalance = brokerLoadBalanceMap.putIfAbsent(topic, brokerLoadBalance);
            if (oldBrokerLoadBalance != null) {
                brokerLoadBalance = oldBrokerLoadBalance;
            }
        }
        return brokerLoadBalance;
    }


    private BrokerLoadBalance create(String loadBalanceType) {
        BrokerLoadBalance loadBalance = Plugins.LOADBALANCE.get(loadBalanceType);
        Preconditions.checkArgument(loadBalance != null, String.format("no balance found. type: %s", loadBalanceType));
        return loadBalance;
    }
}