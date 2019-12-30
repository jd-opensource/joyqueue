package org.joyqueue.broker.producer.transaction;

import org.joyqueue.message.BrokerPrepare;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/12/2
 */
public class BrokerPrepareContext {
    //开始时间
    private long startTime;
    //事物ID
    private String txId;
    //事物查询标识
    private String queryId;

    private String topic;

    private String app;

    private long timeout;

    private byte source;

    private short partition;

    public static BrokerPrepareContext fromBrokerPrepare(BrokerPrepare brokerPrepare) {
        BrokerPrepareContext context = new BrokerPrepareContext();
        context.setApp(brokerPrepare.getApp());
        context.setQueryId(brokerPrepare.getQueryId());
        context.setSource(brokerPrepare.getSource());
        context.setStartTime(brokerPrepare.getStartTime());
        context.setTimeout(brokerPrepare.getTimeout());
        context.setTopic(brokerPrepare.getTopic());
        context.setTxId(brokerPrepare.getTxId());
        context.setPartition(brokerPrepare.getPartition());
        return context;
    }

    public static BrokerPrepareContext fromMap(Map<String, String> map) {
        BrokerPrepareContext context = new BrokerPrepareContext();
        context.setApp(map.get("app"));
        context.setQueryId(map.get("queryId"));
        context.setSource(new Byte(map.get("source")));
        context.setStartTime(new Long(map.get("startTime")));
        context.setTimeout(new Long(map.get("timeout")));
        context.setTopic(map.get("topic"));
        context.setTxId(map.get("txId"));
        context.setPartition(new Short(map.get("partition")));
        return context;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public byte getSource() {
        return source;
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(7);
        map.put("startTime", String.valueOf(startTime));
        map.put("txId", txId);
        map.put("queryId", queryId);
        map.put("topic", topic);
        map.put("app", app);
        map.put("timeout", String.valueOf(timeout));
        map.put("source", String.valueOf(source));
        map.put("partition", String.valueOf(partition));
        return map;
    }
}
