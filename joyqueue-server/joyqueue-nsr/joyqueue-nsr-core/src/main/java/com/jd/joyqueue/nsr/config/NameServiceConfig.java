/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.joyqueue.nsr.config;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.network.transport.config.ClientConfig;
import com.jd.joyqueue.network.transport.config.TransportConfigSupport;
import com.jd.joyqueue.toolkit.config.PropertySupplier;

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
