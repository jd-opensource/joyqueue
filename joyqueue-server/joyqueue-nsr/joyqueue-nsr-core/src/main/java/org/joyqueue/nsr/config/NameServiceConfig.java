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

import com.google.common.base.Preconditions;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.io.Files;

import java.io.File;

public class NameServiceConfig {
    private ClientConfig clientConfig;
    private PropertySupplier propertySupplier;
    private String dataPath;

    public NameServiceConfig(PropertySupplier propertySupplier) {
        Preconditions.checkArgument(propertySupplier != null, "property supplier can not be null.");
        this.propertySupplier = propertySupplier;
        this.clientConfig = TransportConfigSupport.buildClientConfig(propertySupplier, NameServiceConfigKey.NAMESERVICE_KEY_PREFIX);
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public String getNameserverAddress() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ADDRESS);
    }

    public int getHandlerThreads() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_HANDLER_THREADS);
    }

    public int getHandlerQueues() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_HANDLER_QUEUES);
    }

    public int getHandlerKeepalive() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_HANDLER_KEEPALIVE);
    }

    public int getAllMetadataTransportTimeout() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_TRANSPORT_TIMEOUT);
    }

    public boolean getAllMetadataCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_CACHE_ENABLE);
    }

    public String getAllMetadataCacheFile() {
        return getAndCreateDataPath() + PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_CACHE_FILE);
    }

    public int getAllMetadataCacheExpireTime() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_CACHE_EXPIRE_TIME);
    }

    public int getAllMetadataInterval() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_INTERVAL);
    }

    public boolean getAllMetadataRewriteEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_REWRITE_ENABLE);
    }

    public boolean getCompensationEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_ENABLE);
    }

    public int getCompensationInterval() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_INTERVAL);
    }

    public int getCompensationRetryTimes() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_RETRY_TIMES);
    }

    public int getCompensationRetryInterval() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_RETRY_INTERVAL);
    }

    public boolean getCompensationTopicEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_TOPIC_ENABLE);
    }

    public boolean getCompensationBrokerEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_BROKER_ENABLE);
    }

    public boolean getCompensationProducerEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_PRODUCER_ENABLE);
    }

    public boolean getCompensationConsumerEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_CONSUMER_ENABLE);
    }

    public boolean getCompensationDataCenterEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_DATACENTER_ENABLE);
    }

    public boolean getCompensationConfigEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_CONFIG_ENABLE);
    }

    public boolean getCompensationEventEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_EVENT_ENABLE);
    }

    public boolean getCompensationCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_CACHE_ENABLE);
    }

    public boolean getCompensationErrorCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_ERROR_CACHE_ENABLE);
    }

    public int getCompensationErrorThreshold() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_ERROR_THRESHOLD);
    }

    public int getCompensationThreshold() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_THRESHOLD);
    }

    public int getCompensationErrorRetryInterval() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_ERROR_RETRY_INTERVAL);
    }

    public String getMessengerType() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_TYPE);
    }

    public boolean getMessengerIgniteEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_IGNITE_ENABLE);
    }

    public boolean getMessengerPublishLeaderChangeEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_PUBLISH_LEADER_CHANGE_ENABLE);
    }

    public boolean getMessengerPublishTopicEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_PUBLISH_TOPIC_ENABLE);
    }

    public boolean getMessengerPublishLeaderReportEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_PUBLISH_LEADER_REPORT_ENABLE);
    }

    public boolean getMessengerPublishSubscriptionEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_PUBLISH_SUBSCRIPTION_ENABLE);
    }

    public boolean getMessengerPublishBrokerEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_BROKER_ENABLE);
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

    // TODO 代码重复
    protected String getAndCreateDataPath() {
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
                    dataFile = new File(new File(System.getProperty("user.home")), ".joyqueue");
                } else {
                    dataFile = new File(path);
                }
                Files.createDirectory(dataFile);
                dataPath = dataFile.getPath();
            }
        }

        return dataPath;
    }
}
