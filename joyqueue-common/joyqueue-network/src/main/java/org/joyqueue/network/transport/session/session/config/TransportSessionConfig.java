package org.joyqueue.network.transport.session.session.config;

import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransportSessionConfig
 *
 * author: gaohaoxiang
 * date: 2018/11/9
 */
public class TransportSessionConfig {

    protected static final Logger logger = LoggerFactory.getLogger(TransportSessionConfig.class);

    private PropertySupplier propertySupplier;

    public TransportSessionConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getReconnectInterval() {
        return PropertySupplier.getValue(propertySupplier, TransportSessionConfigKey.RECONNECT_INTERVAL);
    }

    public int getSessionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, TransportSessionConfigKey.EXPIRE_TIME);
    }
}