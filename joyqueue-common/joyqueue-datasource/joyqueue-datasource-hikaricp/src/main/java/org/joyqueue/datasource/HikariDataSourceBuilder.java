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
package org.joyqueue.datasource;


/**
 * HikariCP实现
 */
public class HikariDataSourceBuilder implements DataSourceBuilder {
    @Override
    public XDataSource build(DataSourceConfig config) {
        HikariXDataSource ds = new HikariXDataSource();
        ds.setDriverClassName(config.getDriver());
        ds.setJdbcUrl(config.getUrl());
        ds.setUsername(config.getUser());
        ds.setPassword(config.getPassword());
        ds.setConnectionTimeout(config.getConnectionTimeout());
        ds.setIdleTimeout(config.getIdleTimeout());
        ds.setMaxLifetime(config.getMaxLifetime());
        ds.setMaximumPoolSize(config.getMaxPoolSize());
        ds.setMinimumIdle(config.getMinIdle());
        ds.setConnectionTestQuery(config.getValidationQuery());
        ds.setAutoCommit(config.isAutoCommit());
        ds.setTransactionIsolation(config.getTransactionIsolation());
        ds.setReadOnly(config.isReadOnly());
        ds.setCatalog(config.getCatalog());

        //TODO
       /* ds.setInitializationFailFast(false);*/
        System.setProperty("com.zaxxer.hikari.housekeeping.periodMs", String.valueOf(config.getCleanupInterval()));

        if (config.getConnectionProperties() != null) {
            String[] properties = config.getConnectionProperties().split(";");
            int pos;
            String name, value;
            for (String property : properties) {
                pos = property.indexOf('=');
                if (pos > 0 && pos < property.length() - 1) {
                    name = property.substring(0, pos);
                    value = property.substring(pos + 1);
                    ds.addDataSourceProperty(name, value);
                }
            }
        }
        return ds;
    }

    @Override
    public Object type() {
        return "HikariCP";
    }
}
