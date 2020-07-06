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
package org.joyqueue.broker.manage.config;

import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.helper.PortHelper;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * broker监控配置
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class BrokerManageConfig {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerManageConfig.class);

    private BrokerConfig brokerConfig;
    private PropertySupplier propertySupplier;

    public BrokerManageConfig() {
    }

    public BrokerManageConfig(PropertySupplier propertySupplier,BrokerConfig brokerConfig) {
        this.propertySupplier = propertySupplier;
        this.brokerConfig = brokerConfig;
    }

    public int getExportPort() {
        return PortHelper.getMonitorPort(propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_PORT));
    }
}