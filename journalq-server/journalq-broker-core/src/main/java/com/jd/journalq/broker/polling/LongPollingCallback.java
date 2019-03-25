package com.jd.journalq.broker.polling;

import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.common.network.session.Consumer;
import com.jd.journalq.common.network.transport.exception.TransportException;

/**
 * 长轮询回调
 *
 * Created by chengzhiliang on 2018/9/5.
 */
public interface LongPollingCallback {

    public void onSuccess(Consumer consumer, PullResult pullResult) throws TransportException;

    public void onExpire(Consumer consumer) throws TransportException;

    public void onException(Consumer consumer, Throwable throwable) throws TransportException;
}
