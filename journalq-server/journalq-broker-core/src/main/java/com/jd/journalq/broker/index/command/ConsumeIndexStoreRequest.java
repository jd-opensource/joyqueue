package com.jd.journalq.broker.index.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.broker.index.model.IndexAndMetadata;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexStoreRequest extends JMQPayload {
    private String app;
    private Map<String, Map<Integer, IndexAndMetadata>> indexMetadata;

    public ConsumeIndexStoreRequest(String app, Map<String, Map<Integer, IndexAndMetadata>> indexMetadata) {
        this.app = app;
        this.indexMetadata = indexMetadata;
    }

    public String getApp() {
        return app;
    }

    public Map<String, Map<Integer, IndexAndMetadata>> getIndexMetadata() {
        return indexMetadata;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
