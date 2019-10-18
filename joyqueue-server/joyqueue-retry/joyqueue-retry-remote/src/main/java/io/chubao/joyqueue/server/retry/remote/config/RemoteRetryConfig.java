package io.chubao.joyqueue.server.retry.remote.config;

import io.chubao.joyqueue.toolkit.config.PropertySupplier;

/**
 * RemoteRetryConfig
 * author: gaohaoxiang
 * date: 2019/10/14
 */
public class RemoteRetryConfig {

    private PropertySupplier propertySupplier;

    public RemoteRetryConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getLimitThreads() {
        return propertySupplier.getValue(RemoteRetryConfigKey.REMOTE_RETRY_LIMIT_THREADS);
    }

    public long getUpdateInterval() {
        return propertySupplier.getValue(RemoteRetryConfigKey.REMOTE_RETRY_UPDATE_INTERVAL);
    }

    public int getTransportTimeout() {
        return propertySupplier.getValue(RemoteRetryConfigKey.REMOTE_RETRY_TRANSPORT_TIMEOUT);
    }
}