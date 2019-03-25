package com.jd.journalq.broker.kafka.command;


import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.FetchResponsePartitionData;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class FetchResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, FetchResponsePartitionData> fetchResponses;

    public Table<String, Integer, FetchResponsePartitionData> getFetchResponses() {
        return fetchResponses;
    }

    public void setFetchResponses(Table<String, Integer, FetchResponsePartitionData> fetchResponses) {
        this.fetchResponses = fetchResponses;
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }

    @Override
    public String toString() {
        StringBuilder responseStringBuilder = new StringBuilder();
        responseStringBuilder.append("Name: " + this.getClass().getSimpleName());
        responseStringBuilder.append("fetchResponses: " + fetchResponses);
        return responseStringBuilder.toString();
    }
}