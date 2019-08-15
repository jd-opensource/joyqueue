package io.chubao.joyqueue.broker.protocol.converter;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.network.domain.BrokerNode;
import org.apache.commons.lang3.StringUtils;

/**
 * BrokerNodeConverter
 *
 * author: gaohaoxiang
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

        if (Broker.PermissionEnum.FULL.equals(broker.getPermission())) {
            result.setReadable(true);
            result.setWritable(true);
        } else if (Broker.PermissionEnum.READ.equals(broker.getPermission())) {
            result.setReadable(true);
        } else if (Broker.PermissionEnum.WRITE.equals(broker.getPermission())) {
            result.setWritable(true);
        }

        return result;
    }
}