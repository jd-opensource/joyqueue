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
package io.chubao.joyqueue.nsr.config;

import com.google.common.base.Preconditions;
import io.chubao.joyqueue.network.transport.config.ClientConfig;
import io.chubao.joyqueue.network.transport.config.TransportConfigSupport;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

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

    public String getNameserverAddress() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ADDRESS);
    }

    public boolean getAllMetadataCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_CACHE_ENABLE);
    }

    public int getAllMetadataCacheExpireTime() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_ALL_METADATA_CACHE_EXPIRE_TIME);
    }

    public int getCompensationInterval() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_INTERVAL);
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

    public boolean getCompensationCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_COMPENSATION_CACHE_ENABLE);
    }

    public String getMessengerType() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_TYPE);
    }

    public boolean getMessengerIgniteEnable() {
        return PropertySupplier.getValue(propertySupplier, NameServiceConfigKey.NAMESERVER_MESSENGER_IGNITE_ENABLE);
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
}
