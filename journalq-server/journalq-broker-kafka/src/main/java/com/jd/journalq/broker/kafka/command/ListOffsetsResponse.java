package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.PartitionOffsetsResponse;

/**
 * ListOffsetsHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ListOffsetsResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, PartitionOffsetsResponse> offsetsResponseTable;

    public ListOffsetsResponse() {

    }

    public ListOffsetsResponse(Table<String, Integer, PartitionOffsetsResponse> offsetsResponseTable) {
        this.offsetsResponseTable = offsetsResponseTable;
    }

    public void setOffsetsResponseTable(Table<String, Integer, PartitionOffsetsResponse> offsetsResponseTable) {
        this.offsetsResponseTable = offsetsResponseTable;
    }

    public Table<String, Integer, PartitionOffsetsResponse> getOffsetsResponseTable() {
        return offsetsResponseTable;
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: " + this.getClass().getSimpleName());
        return builder.toString();
    }
}
