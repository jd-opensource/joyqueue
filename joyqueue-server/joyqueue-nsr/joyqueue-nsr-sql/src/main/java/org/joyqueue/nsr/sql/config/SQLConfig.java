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
package org.joyqueue.nsr.sql.config;

import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * SQLConfig
 * author: gaohaoxiang
 * date: 2019/8/14
 */
public class SQLConfig {

    protected static final Logger logger = LoggerFactory.getLogger(SQLConfig.class);

    private PropertySupplier propertySupplier;

    public SQLConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getDataSourceType() {
        return PropertySupplier.getValue(propertySupplier, SQLConfigKey.DATASOURCE_TYPE);
    }

    public String getDataSourceClass() {
        return PropertySupplier.getValue(propertySupplier, SQLConfigKey.DATASOURCE_CLASS);
    }

    public Properties getDataSourceProperties() {
        Properties properties = new Properties();
        for (Property property : propertySupplier.getProperties()) {
            if (property.getKey().startsWith(SQLConfigKey.DATASOURCE_PROPERTIES_PREFIX.getName())) {
                properties.put(property.getKey(), String.valueOf(property.getValue()));
            }
        }
        properties.put(SQLConfigKey.DATASOURCE_CLASS.getName(), getDataSourceClass());
        return properties;
    }
}