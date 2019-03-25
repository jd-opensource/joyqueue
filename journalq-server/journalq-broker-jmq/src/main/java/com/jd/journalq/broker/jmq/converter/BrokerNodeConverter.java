package com.jd.journalq.broker.jmq.converter;

import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.domain.DataCenter;
import com.jd.journalq.common.network.domain.BrokerNode;
import org.apache.commons.lang3.StringUtils;

/**
 * BrokerNodeConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class BrokerNodeConverter {

    public static BrokerNode convertBrokerNode(Broker broker) {
        return convertBrokerNode(broker, null, null);
    }

    public static BrokerNode convertBrokerNode(Broker broker, DataCenter brokerDataCenter, String region) {
        return convertBrokerNode(broker, brokerDataCenter, region, 0);
    }

    public static BrokerNode convertBrokerNode(Broker broker, DataCenter brokerDataCenter, String region, int weight) {
        BrokerNode result = new BrokerNode();
        result.setId(broker.getId());
        result.setHost(broker.getIp());
        result.setPort(broker.getPort());
        result.setDataCenter(brokerDataCenter == null ? null : brokerDataCenter.getRegion());

        if (StringUtils.isBlank(region) || brokerDataCenter == null) {
            result.setNearby(true);
        } else {
            result.setNearby(StringUtils.equalsIgnoreCase(brokerDataCenter.getRegion(), region));
        }
        result.setWeight(weight);
        return result;
    }
}