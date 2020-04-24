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
package org.joyqueue.broker.config;

import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.domain.Broker;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.io.Files;
import org.joyqueue.toolkit.network.IpUtil;

import java.io.File;

/**
 * Broker服务配置
 */
public class BrokerConfig implements PropertySupplierAware {
    public static final String BROKER_FRONTEND_SERVER_CONFIG_PREFIX = "broker.frontend-server.";
    public static final String BROKER_BACKEND_SERVER_CONFIG_PREFIX = "broker.backend-server.";
    public static final String BROKER_ID_FILE_NAME = "broker.id";
    public static final String ADMIN_USER = "broker.joyqueue.admin";
    public static final String DEFAULT_DATA_DIR = ".joyqueue";
    public static final int INVALID_BROKER_ID = -1;

    /**
     * broker data root dir
     */
    private String dataPath;
    /**
     * broker
     */
    private Broker broker;
    /**
     * local ip
     */
    private String localIp;

    private String adminUser;

    /**
     * broker fronted server config
     */
    private ServerConfig frontendConfig;

    /**
     * broker backend server config
     */
    private ServerConfig backendConfig;
    /**
     * property supplier
     */
    private Configuration propertySupplier;


    public BrokerConfig(PropertySupplier propertySupplier) {
        setSupplier(propertySupplier);
    }

    public String getAndCreateDataPath() {
        if (dataPath != null) {
            return dataPath;
        }

        // 只能初始化一次
        synchronized (this) {
            if (dataPath == null) {
                Property property = propertySupplier == null ? null : propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
                String path = property == null ? null : property.getString();
                File dataFile;
                if(path == null) {
                    dataFile = new File(new File(System.getProperty("user.home")), DEFAULT_DATA_DIR);
                } else {
                    dataFile = new File(path);
                }
                Files.createDirectory(dataFile);
                dataPath = dataFile.getPath();
                propertySupplier.addProperty(Property.APPLICATION_DATA_PATH, dataPath);

            }
        }

        return dataPath;
    }

    public String getBrokerIdFilePath() {
        return getAndCreateDataPath() + File.separator + BROKER_ID_FILE_NAME;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
        frontendConfig.setPort(broker.getPort());
        backendConfig.setPort(broker.getBackEndPort());
    }


    public Integer getBrokerId() {
        return broker == null ? INVALID_BROKER_ID : broker.getId();
    }


    @Deprecated
    public String getBrokerIp() {
        if (localIp == null) {
            localIp = IpUtil.getLocalIp();
        }
        return localIp;
    }

    @Override
    public void setSupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = (Configuration) propertySupplier;
        this.frontendConfig = TransportConfigSupport.buildServerConfig(propertySupplier, BROKER_FRONTEND_SERVER_CONFIG_PREFIX);
        this.backendConfig = TransportConfigSupport.buildServerConfig(propertySupplier, BROKER_BACKEND_SERVER_CONFIG_PREFIX);
        Property adminUser = propertySupplier.getProperty(ADMIN_USER);
        if (null != adminUser) this.adminUser = adminUser.getString();
    }

    public boolean getServerShardedThreads() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_SHARDED_THREADS);
    }

    public int getServerCommonThreads() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_COMMON_THREADS);
    }

    public int getServerCommonThreadKeepalive() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_COMMON_THREAD_KEEPALIVE);
    }

    public int getServerCommonThreadQueueSize() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_COMMON_THREAD_QUEUE_SIZE);
    }

    public int getServerFetchThreads() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_FETCH_THREADS);
    }

    public int getServerFetchThreadKeepalive() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_FETCH_THREAD_KEEPALIVE);
    }

    public int getServerFetchThreadQueueSize() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_FETCH_THREAD_QUEUE_SIZE);
    }

    public int getServerProduceThreads() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_PRODUCE_THREADS);
    }

    public int getServerProduceThreadKeepalive() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_PRODUCE_THREAD_KEEPALIVE);
    }

    public int getServerProduceThreadQueueSize() {
        return propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_PRODUCE_THREAD_QUEUE_SIZE);
    }

    public boolean getLogDetail(String app) {
        return (boolean) propertySupplier.getValue(BrokerConfigKey.LOG_DETAIL)
                || (boolean) PropertySupplier.getValue(propertySupplier,
                BrokerConfigKey.LOG_DETAIL_PREFIX.getName() + app,
                BrokerConfigKey.LOG_DETAIL_PREFIX.getType(),
                BrokerConfigKey.LOG_DETAIL_PREFIX.getValue());
    }

    public Broker getBroker() {
        return broker;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public ServerConfig getFrontendConfig() {
        return frontendConfig;
    }

    public void setFrontendConfig(ServerConfig frontendConfig) {
        this.frontendConfig = frontendConfig;
    }

    public ServerConfig getBackendConfig() {
        return backendConfig;
    }

    public void setBackendConfig(ServerConfig backendConfig) {
        this.backendConfig = backendConfig;
    }
}
