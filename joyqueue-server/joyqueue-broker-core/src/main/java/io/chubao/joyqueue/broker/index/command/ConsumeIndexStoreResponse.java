package io.chubao.joyqueue.broker.index.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;
import io.chubao.joyqueue.network.command.CommandType;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexStoreResponse extends JoyQueuePayload {
    private Map<String, Map<Integer, Short>> indexStoreStatus;

    public ConsumeIndexStoreResponse(Map<String, Map<Integer, Short>> indexStoreStatus) {
        this.indexStoreStatus = indexStoreStatus;
    }

    public Map<String, Map<Integer, Short>> getIndexStoreStatus() {
        return indexStoreStatus;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_RESPONSE;
    }
}
