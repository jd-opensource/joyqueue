package com.jd.journalq.broker.kafka.coordinator.transaction.helper;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.Set;

/**
 * TransactionHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/18
 */
public class TransactionHelper {

    public static Set<TransactionPrepare> filterPrepareByBroker(Set<TransactionPrepare> prepareList) {
        if (CollectionUtils.isEmpty(prepareList)) {
            return Collections.emptySet();
        }
        Table<Integer, String, Boolean> brokerTopicTable = HashBasedTable.create();
        Set<TransactionPrepare> result = Sets.newHashSet();
        for (TransactionPrepare prepare : prepareList) {
            if (brokerTopicTable.contains(prepare.getBrokerId(), prepare.getTopic())) {
                continue;
            }
            brokerTopicTable.put(prepare.getBrokerId(), prepare.getTopic(), true);
            result.add(prepare);
        }
        return result;
    }
}