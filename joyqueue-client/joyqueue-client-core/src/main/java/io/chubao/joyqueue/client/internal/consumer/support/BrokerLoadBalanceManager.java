package io.chubao.joyqueue.client.internal.consumer.support;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.client.internal.consumer.BrokerLoadBalance;
import com.google.common.base.Preconditions;
import io.chubao.joyqueue.client.internal.Plugins;

import java.util.concurrent.ConcurrentMap;

/**
 * BrokerLoadBalanceManager
 *
 * author: gaohaoxiang
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