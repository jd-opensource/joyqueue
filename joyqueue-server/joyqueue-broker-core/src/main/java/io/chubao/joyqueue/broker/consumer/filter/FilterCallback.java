package io.chubao.joyqueue.broker.consumer.filter;

import io.chubao.joyqueue.exception.JoyQueueException;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 消息过滤回调
 *
 * Created by chengzhiliang on 2019/2/20.
 */
public interface FilterCallback {

    void callback(List<ByteBuffer> list) throws JoyQueueException;
}
