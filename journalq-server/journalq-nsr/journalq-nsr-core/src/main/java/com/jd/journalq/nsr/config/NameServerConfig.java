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
package com.jd.journalq.nsr.config;

import com.jd.journalq.network.transport.config.ServerConfig;
import com.jd.journalq.network.transport.config.TransportConfigSupport;
import com.jd.journalq.toolkit.config.PropertySupplier;

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
