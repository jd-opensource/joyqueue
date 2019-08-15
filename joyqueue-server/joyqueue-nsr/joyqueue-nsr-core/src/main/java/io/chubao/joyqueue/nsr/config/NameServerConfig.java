package io.chubao.joyqueue.nsr.config;

import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.chubao.joyqueue.network.transport.config.TransportConfigSupport;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

/**
 * @author lixiaobin6
 * ${time} ${date}
 */
public class NameServerConfig {
    protected ServerConfig serverConfig;
    private PropertySupplier propertySupplier;

    public NameServerConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.serverConfig = TransportConfigSupport.buildServerConfig(propertySupplier, NameServerConfigKey.NAME_SERVER_CONFIG_PREFIX);
    }

    public int getManagerPort() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVER_MANAGE_PORT);
    }

    public int getServicePort() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVER_SERVICE_PORT);
    }

    public boolean getCacheEnable() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVER_CACHE_ENABLE);
    }

    public int getCacheExpireTime() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVER_CACHE_EXPIRE_TIME);
    }

    public String getNameserverAddress() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVER_ADDRESS);
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public String getName() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVICE_NAME);
    }


}
