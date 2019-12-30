package org.joyqueue.broker.network.session;

import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * @author LiYue
 * Date: 2019/12/12
 */
public class BrokerTransportConfig {

    private final PropertySupplier propertySupplier;

    public BrokerTransportConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getSessionTimeout() {
        return PropertySupplier.getValue(propertySupplier, BrokerTransportConfigKey.SESSION_SYNC_TIMEOUT);
    }

    public int getSessionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, BrokerTransportConfigKey.SESSION_EXPIRE_TIME);
    }

}
