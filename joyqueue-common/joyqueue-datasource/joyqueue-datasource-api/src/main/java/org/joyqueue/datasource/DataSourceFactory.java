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


import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;

/**
 * 数据源工厂类
 */
public class DataSourceFactory {
    private ExtensionPoint<DataSourceBuilder, String> builders = new ExtensionPointLazy<>(DataSourceBuilder.class, SpiLoader.INSTANCE, null, null);


    private DataSourceConfig config;

    public DataSourceFactory(DataSourceConfig config) {
        this.config = config;
    }

    /**
     * 构建数据源
     *
     * @param config 数据源配置
     * @return 数据源
     */
    public static XDataSource build(DataSourceConfig config) {
        if (config == null) {
            return null;
        }
        DataSourceFactory factory = new DataSourceFactory(config);
        return factory.build();
    }

    /**
     * 构建数据源
     *
     * @return 数据源
     */
    public XDataSource build() {
        DataSourceBuilder builder = builders.get(config.getType());
        if (builder == null) {
            return null;
        }
        return builder.build(config);
    }

}
