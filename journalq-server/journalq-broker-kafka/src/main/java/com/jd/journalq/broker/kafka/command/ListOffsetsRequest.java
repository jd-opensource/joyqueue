package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.IsolationLevel;
import com.jd.journalq.domain.TopicName;

/**
 * Created by zhuduohui on 2018/9/10.
 */
public class ListOffsetsRequest extends KafkaRequestOrResponse {

    private int replicaId;
    private IsolationLevel isolationLevel;
    private Table<TopicName, Integer, PartitionOffsetRequestInfo> offsetRequestTable;


    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(IsolationLevel isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public void setOffsetRequestTable(Table<TopicName, Integer, PartitionOffsetRequestInfo> offsetRequestTable) {
        this.offsetRequestTable = offsetRequestTable;
    }

    public Table<TopicName, Integer, PartitionOffsetRequestInfo> getOffsetRequestTable() {
        return offsetRequestTable;
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }

    @Override
    public String toString() {
        return describe();
    }

    public String describe() {
        StringBuilder offsetRequest = new StringBuilder();
        offsetRequest.append("Name: " + this.getClass().getSimpleName());
        offsetRequest.append("; ReplicaId: " + replicaId);
        return offsetRequest.toString();
    }

    public class PartitionOffsetRequestInfo {

        private long time;

        public PartitionOffsetRequestInfo(long time, int maxNumOffsets) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

    }
}
