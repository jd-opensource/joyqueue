package com.jd.journalq.client.internal.producer.support;

import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.producer.PartitionSelector;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.client.internal.Plugins;

import java.util.concurrent.ConcurrentMap;

/**
 * PartitionSelectorManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class PartitionSelectorManager {

    private ConcurrentMap<String, PartitionSelector> partitionSelectorMap = Maps.newConcurrentMap();

    public PartitionSelector getPartitionSelector(String topic, String selectorType) {
        PartitionSelector partitionSelector = partitionSelectorMap.get(topic);
        if (partitionSelector == null) {
            partitionSelector = create(selectorType);
            PartitionSelector oldPartitionSelector = partitionSelectorMap.putIfAbsent(topic, partitionSelector);
            if (oldPartitionSelector != null) {
                partitionSelector = oldPartitionSelector;
            }
        }
        return partitionSelector;
    }

    private PartitionSelector create(String selectorType) {
        PartitionSelector selector = Plugins.PARTITION_SELECTOR.get(selectorType);
        Preconditions.checkArgument(selector != null, String.format("no partition selector found. type: %s", selectorType));
        return selector;
    }
}