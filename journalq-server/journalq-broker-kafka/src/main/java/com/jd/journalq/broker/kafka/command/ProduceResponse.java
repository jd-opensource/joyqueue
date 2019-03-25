package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.ProducePartitionStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-1.
 */
public class ProduceResponse extends KafkaRequestOrResponse {

    private Map<String, List<ProducePartitionStatus>> producerResponseStatuss;

    public ProduceResponse() {

    }

    public ProduceResponse(Map<String, List<ProducePartitionStatus>> producerResponseStatuss) {
        this.producerResponseStatuss = producerResponseStatuss;
    }

    public Map<String, List<ProducePartitionStatus>> getProducerResponseStatuss() {
        return producerResponseStatuss;
    }

    public void setProducerResponseStatuss(Map<String, List<ProducePartitionStatus>> producerResponseStatuss) {
        this.producerResponseStatuss = producerResponseStatuss;
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }
}