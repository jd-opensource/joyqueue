package io.chubao.joyqueue.server.retry;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.server.retry.api.MessageRetry;
import io.chubao.joyqueue.server.retry.api.RetryPolicyProvider;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

import java.util.List;

/**
 * Created by chengzhiliang on 2019/4/11.
 */
public class NullMessageRetry implements MessageRetry<Long> {
    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException {

    }

    @Override
    public void retrySuccess(String topic, String app, Long[] messageIds) throws JoyQueueException {

    }

    @Override
    public void retryError(String topic, String app, Long[] messageIds) throws JoyQueueException {

    }

    @Override
    public void retryExpire(String topic, String app, Long[] messageIds) throws JoyQueueException {

    }

    @Override
    public List<RetryMessageModel> getRetry(String topic, String app, short count, long startIndex) throws JoyQueueException {
        return null;
    }

    @Override
    public int countRetry(String topic, String app) throws JoyQueueException {
        return 0;
    }

    @Override
    public void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider) {

    }

    @Override
    public void setSupplier(PropertySupplier supplier) {

    }
}
