/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.nsr.config;

import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.helper.PortHelper;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * MessengerConfig
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerConfig {

    private ServerConfig serverConfig;
    private ClientConfig clientConfig;
    private PropertySupplier propertySupplier;

    public MessengerConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.serverConfig = TransportConfigSupport.buildServerConfig(propertySupplier, MessengerConfigKey.MESSENGER_SERVER_CONFIG_PREFIX);
        this.clientConfig = TransportConfigSupport.buildClientConfig(propertySupplier, MessengerConfigKey.MESSENGER_CLIENT_CONFIG_PREFIX);
    }

    public int getSessionExpireTime() {
        return propertySupplier.getValue(MessengerConfigKey.SESSION_EXPIRE_TIME);
    }

    public int getSessionTimeout() {
        return propertySupplier.getValue(MessengerConfigKey.SESSION_TIMEOUT);
    }

    public int getPublishTimeout() {
        return propertySupplier.getValue(MessengerConfigKey.PUBLISH_TIMEOUT);
    }

    public boolean getPublishEnable() {
        return propertySupplier.getValue(MessengerConfigKey.PUBLISH_ENABLE);
    }

    public int getHandlerThreads() {
        return propertySupplier.getValue(MessengerConfigKey.HANDLER_THREADS);
    }

    public int getHandlerQueues() {
        return propertySupplier.getValue(MessengerConfigKey.HANDLER_QUEUES);
    }

    public int getHandlerKeepalive() {
        return propertySupplier.getValue(MessengerConfigKey.HANDLER_KEEPALIVE);
    }

    public boolean getPublishForce() {
        return propertySupplier.getValue(MessengerConfigKey.PUBLISH_FORCE);
    }

    public boolean getPublishIgnoreConnectionError() {
        return propertySupplier.getValue(MessengerConfigKey.PUBLISH_IGNORE_CONNECTION_ERROR);
    }

    public int getHeartbeatTimeout() {
        return propertySupplier.getValue(MessengerConfigKey.HEARTBEAT_TIMEOUT);
    }

    public int getHeartbeatInterval() {
        return propertySupplier.getValue(MessengerConfigKey.HEARTBEAT_INTERVAL);
    }

    public int getPort() {
        return PortHelper.getMessengerPort(propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_PORT));
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public String getName() {
        return propertySupplier.getValue(NameServerConfigKey.NAMESERVICE_NAME);
    }
}