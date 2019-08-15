package io.chubao.joyqueue.broker.polling;

import io.chubao.joyqueue.broker.consumer.model.PullResult;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.network.transport.exception.TransportException;

/**
 * 长轮询回调
 *
 * Created by chengzhiliang on 2018/9/5.
 */
public interface LongPollingCallback {

    void onSuccess(Consumer consumer, PullResult pullResult) throws TransportException;

    void onExpire(Consumer consumer) throws TransportException;

    void onException(Consumer consumer, Throwable throwable) throws TransportException;
}
