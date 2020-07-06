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
package org.joyqueue.network.transport.config;

import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;

import java.util.List;

public class TransportConfigSupport {

    public static ServerConfig buildServerConfig(final PropertySupplier propertySupplier, String keyPrefix) {
        ServerConfig serverConfig = new ServerConfig();

        buildTransportConfig(propertySupplier, keyPrefix, serverConfig);
        List<Property> properties = propertySupplier.getPrefix(keyPrefix);
        if (properties != null) {
            for (Property property : properties) {
                if (property.getKey().equals(keyPrefix + TRANSPORT_SERVER_PORT)) {
                    serverConfig.setPort(property.getInteger());
                } else if (property.getKey().equals(keyPrefix + TRANSPORT_SERVER_HOST)) {
                    serverConfig.setHost(property.getString());
                }
            }
        }
        return serverConfig;
    }

    public static ClientConfig buildClientConfig(final PropertySupplier propertySupplier, String keyPrefix) {
        ClientConfig clientConfig = new ClientConfig();

        buildTransportConfig(propertySupplier, keyPrefix, clientConfig);
        List<Property> properties = propertySupplier.getPrefix(keyPrefix);
        if (properties != null) {
            for (Property property : properties) {
                if (property.getKey().equals(keyPrefix + TRANSPORT_CLIENT_IPV6)) {
                    clientConfig.setPreferIPv6(property.getBoolean());
                } else if (property.getKey().equals(keyPrefix + TRANSPORT_CLIENT_CONNECT_TIMEOUT)) {
                    clientConfig.setConnectionTimeout(property.getInteger());
                }
            }
        }
        return clientConfig;
    }


    public static final TransportConfig buildTransportConfig(final PropertySupplier propertySupplier, String keyPrefix, final TransportConfig transportConfig) {

        List<Property> properties = propertySupplier.getPrefix(keyPrefix);

        if (properties != null && !properties.isEmpty()) {
            for (Property property : properties) {
                String fullKey = property.getKey();
                if (fullKey.equals(keyPrefix + TRANSPORT_HOST)) {
                    transportConfig.setHost(property.getString());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_ACCEPT_THREAD)) {
                    transportConfig.setAcceptThread(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_IO_THREAD)) {
                    transportConfig.setIoThread(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_MAX_IDLE_TIME)) {
                    transportConfig.setMaxIdleTime(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_REUSE_ADDRESS)) {
                    transportConfig.setReuseAddress(property.getBoolean());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_SO_LINGER)) {
                    transportConfig.setSoLinger(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_TCP_NO_DELAY)) {
                    transportConfig.setTcpNoDelay(property.getBoolean());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_KEEP_ALIVE)) {
                    transportConfig.setKeepAlive(property.getBoolean());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_SOCKET_TIME_OUT)) {
                    transportConfig.setSoTimeout(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_SOCKET_BUFFER_SIZE)) {
                    transportConfig.setSocketBufferSize(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_FRAME_MAX_SZE)) {
                    transportConfig.setFrameMaxSize(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_BACKLOG)) {
                    transportConfig.setBacklog(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_MAX_ONEWAY)) {
                    transportConfig.setMaxOneway(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_MAX_ASYNC)) {
                    transportConfig.setMaxAsync(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_CALL_BACK_THREADS)) {
                    transportConfig.setCallbackThreads(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_CALL_NON_BLOCK_ONEWAY)) {
                    transportConfig.setNonBlockOneway(property.getBoolean());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_CALL_NON_BLOCK_ASYNC)) {
                    transportConfig.setNonBlockAsync(property.getBoolean());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_SEND_TIMEOUT)) {
                    transportConfig.setSendTimeout(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_RETRY_DELAY)) {
                    transportConfig.getRetryPolicy().setRetryDelay(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_RETRY_MAX)) {
                    transportConfig.getRetryPolicy().setMaxRetrys(property.getInteger());
                } else if (fullKey.equals(keyPrefix + TRANSPORT_CLEAR_INTERVAL)) {
                    transportConfig.setClearInterval(property.getInteger());
                }
            }

        }
        return transportConfig;

    }

    //common transport config
    public static final String TRANSPORT_HOST = "transport.host";
    public static final String TRANSPORT_ACCEPT_THREAD = "transport.acceptThreads";
    public static final String TRANSPORT_IO_THREAD = "transport.ioThreads";
    public static final String TRANSPORT_MAX_IDLE_TIME = "transport.maxIdleTime";
    public static final String TRANSPORT_REUSE_ADDRESS = "transport.reuseAddress";
    public static final String TRANSPORT_SO_LINGER = "transport.soLinger";
    public static final String TRANSPORT_TCP_NO_DELAY = "transport.tcpNoDelay";
    public static final String TRANSPORT_KEEP_ALIVE = "transport.keepAlive";
    public static final String TRANSPORT_SOCKET_TIME_OUT = "transport.socketTimeout";
    public static final String TRANSPORT_SOCKET_BUFFER_SIZE = "transport.socketBufferSize";
    public static final String TRANSPORT_FRAME_MAX_SZE = "transport.frameMaxSize";
    public static final String TRANSPORT_BACKLOG = "transport.backlog";
    public static final String TRANSPORT_MAX_ONEWAY = "transport.maxOneWay";
    public static final String TRANSPORT_MAX_ASYNC = "transport.maxAsync";
    public static final String TRANSPORT_CALL_BACK_THREADS = "transport.callbackThreads";
    public static final String TRANSPORT_CALL_NON_BLOCK_ONEWAY = "transport.nonBlockOneway";
    public static final String TRANSPORT_CALL_NON_BLOCK_ASYNC = "transport.nonBlockAsync";
    public static final String TRANSPORT_SEND_TIMEOUT = "transport.sendTimeout";
    public static final String TRANSPORT_RETRY_DELAY = "transport.retryDelay";
    public static final String TRANSPORT_RETRY_MAX = "transport.retryMax";
    public static final String TRANSPORT_CLEAR_INTERVAL = "transport.clearInterval";

    //server transport config
    public static final String TRANSPORT_SERVER_PORT = "transport.server.port";
    public static final String TRANSPORT_SERVER_HOST = "transport.server.host";

    //client transport config
    public static final String TRANSPORT_CLIENT_CONNECT_TIMEOUT = "transport.client.connectTimeout";
    public static final String TRANSPORT_CLIENT_IPV6 = "transport.client.ipv6";

}
