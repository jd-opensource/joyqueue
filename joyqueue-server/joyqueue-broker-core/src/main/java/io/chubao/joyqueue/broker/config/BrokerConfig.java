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
package io.chubao.joyqueue.broker.config;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.chubao.joyqueue.network.transport.config.TransportConfigSupport;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.io.Files;
import io.chubao.joyqueue.toolkit.network.IpUtil;

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


    public BrokerConfig(Configuration propertySupplier) {
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
