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
package com.jd.joyqueue.broker.config;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.domain.Broker;
import com.jd.joyqueue.network.transport.config.ServerConfig;
import com.jd.joyqueue.network.transport.config.TransportConfigSupport;
import com.jd.joyqueue.toolkit.config.Property;
import com.jd.joyqueue.toolkit.config.PropertySupplier;
import com.jd.joyqueue.toolkit.config.PropertySupplierAware;
import com.jd.joyqueue.toolkit.io.Files;
import com.jd.joyqueue.toolkit.network.IpUtil;

import java.io.File;

/**
 * Broker服务配置
 */
public class BrokerConfig implements PropertySupplierAware {
    public static final String BROKER_FRONTEND_SERVER_CONFIG_PREFIX = "broker.frontend-server.";
    public static final String BROKER_BACKEND_SERVER_CONFIG_PREFIX = "broker.backend-server.";
    public static final String BROKER_ID_FILE_NAME = "broker.id";
    public static final String ADMIN_USER = "broker.joyqueue.admin";
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
    private PropertySupplier propertySupplier;


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
                Preconditions.checkArgument(path != null, "data path can not be null.");
                Files.createDirectory(new File(path));
                dataPath = path;
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
        this.propertySupplier = propertySupplier;
        this.frontendConfig = TransportConfigSupport.buildServerConfig(propertySupplier, BROKER_FRONTEND_SERVER_CONFIG_PREFIX);
        this.backendConfig = TransportConfigSupport.buildServerConfig(propertySupplier, BROKER_BACKEND_SERVER_CONFIG_PREFIX);
        Property adminUser = propertySupplier.getProperty(ADMIN_USER);
        if (null != adminUser) this.adminUser = adminUser.getString();
    }

    public Broker getBroker() {
        //TODO
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
