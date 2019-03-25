package com.jd.journalq.nsr.config;

import com.jd.journalq.common.network.transport.config.ClientConfig;
import com.jd.journalq.common.network.transport.config.TransportConfigSupport;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.jd.journalq.toolkit.lang.Preconditions;

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

    public void setPropertySupplier(PropertySupplier propertySupplier) {
        if (propertySupplier != null) {
            this.propertySupplier = propertySupplier;
            this.clientConfig = TransportConfigSupport.buildClientConfig(propertySupplier, NameServiceConfigKey.NAMESERVICE_KEY_PREFIX);
        }
    }
}
