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
package org.joyqueue.broker.monitor.config;

import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.toolkit.config.PropertySupplier;


/**
 * broker监控配置
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class BrokerMonitorConfig {
    private BrokerConfig brokerConfig;
    private PropertySupplier propertySupplier;

    public BrokerMonitorConfig(PropertySupplier propertySupplier, BrokerConfig brokerConfig) {
        this.propertySupplier = propertySupplier;
        this.brokerConfig = brokerConfig;
    }

    public boolean isEnable() {
        return propertySupplier.getValue(BrokerMonitorConfigKey.ENABLE);
    }

    public String getStatSaveFile() {
        return brokerConfig.getAndCreateDataPath() + propertySupplier.getValue(BrokerMonitorConfigKey.STAT_SAVE_FILE);
    }

    public String getStatSaveFileNew() {
        return brokerConfig.getAndCreateDataPath() + propertySupplier.getValue(BrokerMonitorConfigKey.STAT_SAVE_FILE_NEW);
    }

    public int getStatSaveInterval() {
        return propertySupplier.getValue(BrokerMonitorConfigKey.STAT_SAVE_INTERVAL);
    }

}