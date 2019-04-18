package com.jd.journalq.broker.kafka.coordinator.transaction.helper;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * TransactionHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/18
 */
public class TransactionHelper {

    public static List<TransactionPrepare> filterPrepareByBroker(List<TransactionPrepare> prepareList) {
        if (CollectionUtils.isEmpty(prepareList)) {
            return Collections.emptyList();
        }
        Table<Integer, String, Boolean> brokerTopicTable = HashBasedTable.create();
        List<TransactionPrepare> result = Lists.newLinkedList();
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