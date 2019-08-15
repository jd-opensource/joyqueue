package io.chubao.joyqueue.broker.index.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.broker.index.model.IndexAndMetadata;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexStoreRequest extends JoyQueuePayload {
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

    @Override
    public String toString() {
        return "ConsumeIndexStoreRequest{" +
                "app='" + app + '\'' +
                ", indexMetadata=" + indexMetadata +
                '}';
    }
}
