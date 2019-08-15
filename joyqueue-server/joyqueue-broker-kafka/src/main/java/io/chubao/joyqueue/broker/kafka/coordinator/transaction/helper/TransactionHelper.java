package io.chubao.joyqueue.broker.kafka.coordinator.transaction.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import io.chubao.joyqueue.domain.Broker;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TransactionHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/18
 */
public class TransactionHelper {

    public static Map<Broker, List<TransactionPrepare>> splitPrepareByBroker(Set<TransactionPrepare> prepareList) {
        if (CollectionUtils.isEmpty(prepareList)) {
            return Collections.emptyMap();
        }
        Map<Broker, List<TransactionPrepare>> result = Maps.newHashMapWithExpectedSize(prepareList.size());
        for (TransactionPrepare prepare : prepareList) {
            Broker broker = new Broker();
            broker.setId(prepare.getBrokerId());
            broker.setIp(prepare.getBrokerHost());
            broker.setPort(prepare.getBrokerPort());

            List<TransactionPrepare> brokerPrepareList = result.get(broker);
            if (brokerPrepareList == null) {
                brokerPrepareList = Lists.newLinkedList();
                result.put(broker, brokerPrepareList);
            }

            brokerPrepareList.add(prepare);
        }
        return result;
    }
}