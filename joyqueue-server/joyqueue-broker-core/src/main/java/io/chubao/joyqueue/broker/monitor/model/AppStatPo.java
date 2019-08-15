package io.chubao.joyqueue.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * app po
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class AppStatPo extends BasePo {

    private Map<Integer, PartitionGroupStatPo> partitionGroupStatMap = Maps.newHashMap();
    private ConsumerStatPo consumerStat;
    private ProducerStatPo producerStat;

    public Map<Integer, PartitionGroupStatPo> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }

    public void setPartitionGroupStatMap(Map<Integer, PartitionGroupStatPo> partitionGroupStatMap) {
        this.partitionGroupStatMap = partitionGroupStatMap;
    }

    public ConsumerStatPo getConsumerStat() {
        return consumerStat;
    }

    public void setConsumerStat(ConsumerStatPo consumerStat) {
        this.consumerStat = consumerStat;
    }

    public ProducerStatPo getProducerStat() {
        return producerStat;
    }

    public void setProducerStat(ProducerStatPo producerStat) {
        this.producerStat = producerStat;
    }
}