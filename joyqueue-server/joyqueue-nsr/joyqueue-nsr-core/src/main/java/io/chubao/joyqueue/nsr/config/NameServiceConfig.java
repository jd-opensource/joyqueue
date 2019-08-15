package io.chubao.joyqueue.nsr.config;

import com.google.common.base.Preconditions;
import io.chubao.joyqueue.network.transport.config.ClientConfig;
import io.chubao.joyqueue.network.transport.config.TransportConfigSupport;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

public class NameServiceConfig {
    private ClientConfig clientConfig;
    private PropertySupplier propertySupplier;

    public NameServiceConfig(PropertySupplier propertySupplier) {
        Preconditions.checkArgument(propertySupplier != null, "property supplier can not be null.");
        this.propertySupplier = propertySupplier;
        this.clientConfig = TransportConfigSupport.buildClientConfig(propertySupplier, NameServiceConfigKey.NAMESERVICE_KEY_PREFIX);
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public String getNamserverAddress() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ADDRESS);
    }

    public int getThinTransportTimeout() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_THIN_TRANSPORT_TIMEOUT);
    }

    public int getThinTransportTopicTimeout() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_THIN_TRANSPORT_TOPIC_TIMEOUT);
    }

    public boolean getThinCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_THIN_CACHE_ENABLE);
    }

    public int getThinCacheExpireTime() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_THIN_CACHE_EXPIRE_TIME);
    }

    public void setPropertySupplier(PropertySupplier propertySupplier) {
        if (propertySupplier != null) {
            this.propertySupplier = propertySupplier;
            this.clientConfig = TransportConfigSupport.buildClientConfig(propertySupplier, NameServiceConfigKey.NAMESERVICE_KEY_PREFIX);
        }
    }
}
