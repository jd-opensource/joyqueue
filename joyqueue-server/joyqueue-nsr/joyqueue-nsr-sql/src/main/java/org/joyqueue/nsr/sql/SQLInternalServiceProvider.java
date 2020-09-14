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
package org.joyqueue.nsr.sql;

import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.nsr.InternalServiceProvider;
import org.joyqueue.nsr.NsrPlugins;
import org.joyqueue.nsr.sql.config.SQLConfig;
import org.joyqueue.nsr.sql.operator.DataSourceFactory;
import org.joyqueue.nsr.sql.operator.SQLOperator;
import org.joyqueue.nsr.sql.operator.support.DefaultDataSourceFactory;
import org.joyqueue.nsr.sql.operator.support.DefaultSQLOperator;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLInternalServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class SQLInternalServiceProvider extends Service implements InternalServiceProvider, PropertySupplierAware {

    protected static final Logger logger = LoggerFactory.getLogger(SQLInternalServiceProvider.class);

    private static final ExtensionPoint<DataSourceFactory, String> DATASOURUCE_FACTORY = new ExtensionPointLazy<>(DataSourceFactory.class, SpiLoader.INSTANCE, null, null);

    private PropertySupplier propertySupplier;
    private SQLConfig config;
    private PointTracer tracer;

    private SQLOperator sqlOperator;
    private SQLInternalServiceManager sqlInternalServiceManager;

    @Override
    public void setSupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.config = new SQLConfig(propertySupplier);
    }

    @Override
    protected void validate() throws Exception {
        this.tracer = NsrPlugins.TRACERERVICE.get(PropertySupplier.getValue(propertySupplier, BrokerConfigKey.TRACER_TYPE));
    }

    @Override
    protected void doStart() throws Exception {
        this.sqlOperator = createSQLOperator(config);
        BatchOperationContext.init(sqlOperator);
        this.sqlInternalServiceManager = new SQLInternalServiceManager(this.sqlOperator, this.tracer);
        this.sqlInternalServiceManager.start();
    }

    protected SQLOperator createSQLOperator(SQLConfig config) {
        DataSourceFactory dataSourceFactory = createDataSourceFactory();
        return new DefaultSQLOperator(config.getDataSourceProperties(), dataSourceFactory);
    }

    protected DataSourceFactory createDataSourceFactory() {
        String type = config.getDataSourceType();
        DataSourceFactory dataSourceFactory = null;
        if (StringUtils.isNotBlank(type)) {
            dataSourceFactory = DATASOURUCE_FACTORY.get(type);
        }
        if (dataSourceFactory == null) {
            return new DefaultDataSourceFactory();
        }
        return dataSourceFactory;
    }

    @Override
    protected void doStop() {
        if (sqlInternalServiceManager != null) {
            sqlInternalServiceManager.stop();
        }
        if (sqlOperator instanceof LifeCycle) {
            ((LifeCycle) sqlOperator).stop();
        }
    }

    @Override
    public <T> T getService(Class<T> service) {
        return sqlInternalServiceManager.getService(service);
    }

    @Override
    public String type() {
        return SQLConsts.TYPE;
    }
}