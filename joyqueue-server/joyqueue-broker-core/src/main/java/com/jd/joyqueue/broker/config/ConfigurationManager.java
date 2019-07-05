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
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.joyqueue.broker.config;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.domain.Config;
import com.jd.joyqueue.event.ConfigEvent;
import com.jd.joyqueue.event.NameServerEvent;
import com.jd.joyqueue.toolkit.concurrent.EventBus;
import com.jd.joyqueue.toolkit.concurrent.EventListener;
import com.jd.joyqueue.toolkit.config.Property;
import com.jd.joyqueue.toolkit.lang.Close;
import com.jd.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.jd.joyqueue.broker.config.Configuration.DEFAULT_CONFIGURATION_PRIORITY;


/**
 * 上下文管理器
 */
public class ConfigurationManager extends Service implements EventListener<NameServerEvent> {
    private static final String DEFAULT_CONFIGURATION_NAME = "_BROKER_CONFIG_";
    private static final String CONFIGURATION_VERSION = "_CONFIGURATION_VERSION_";
    private static final String DEFAULT_CONFIG_PATH = "joyqueue.properties";


    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private ConfigProvider configProvider;
    // 事件派发器
    private EventBus<ConfigEvent> eventManager = new EventBus<ConfigEvent>("ContextManager");

    private Configuration configuration;
    private String configPath = DEFAULT_CONFIG_PATH;

    public ConfigurationManager(String[] args) {

    }
    private void parseParams(Configuration configuration, String[] args) {
        //TODO 解析参数
    }

    public ConfigurationManager(String configPath) {
        if (configPath != null && !configPath.isEmpty()) {
            this.configPath = configPath;
        }
    }

    public Configuration getConfiguration() {
        Preconditions.checkState(isStarted(), "config manager not not started yet.");
        return this.configuration;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        SystemConfigLoader.load();
        if (configuration == null) {
            this.configuration = buildConfiguration();
        }
    }


    private Configuration buildConfiguration() throws Exception {
        Configuration configuration = new Configuration();
        URL url = getClass().getClassLoader().getResource(this.configPath);
        if (null != url) {
            logger.info("Found conf file: {}.", url);
            InputStream in = url.openStream();
            Properties properties = new Properties();
            properties.load(in);
            String text = (String) properties.remove(CONFIGURATION_VERSION);
            long dataVersion = Configuration.DEFAULT_CONFIGURATION_VERSION;
            if (text != null && !text.isEmpty()) {
                try {
                    dataVersion = Long.parseLong(text);
                } catch (NumberFormatException e) {
                }
            }
            List<Property> propertyList = new ArrayList<>(properties.size());
            String key;
            String value;
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                key = entry.getKey().toString();
                value = entry.getValue().toString();
                propertyList.add(new Property(DEFAULT_CONFIGURATION_NAME, key, value, dataVersion, DEFAULT_CONFIGURATION_PRIORITY));
            }
            configuration.addProperties(propertyList);
        } else {
            logger.info("No {} in classpath, using default.", this.configPath);
        }
        return configuration;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
        if (isStarted()) {
            doUpdateConfig();
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        eventManager.start();
        //加载动态配置
        doUpdateConfig();
        logger.info("context manager is started");
    }

    @Override
    protected void doStop() {
        super.doStop();
        Close.close(eventManager);
        logger.info("configuration manager is stopped");
    }


    private void doUpdateConfig() {
        if (configProvider != null) {
            List<Config> configs = configProvider.getConfigs();
            if (null != configs) {
                doUpdateProperty(configs.toArray(new Config[configs.size()]));
            } else {
                logger.warn("no dynamic config found.");
            }
        } else {
            logger.warn("config provider not int yet.");
        }
    }


    private void doUpdateProperty(Config... configs) {
        if (configs != null) {
            for (Config config : configs) {
                logger.info("received config [{}], corresponding property is [{}]", config,configuration.getProperty(config.getKey()) != null ? configuration.getProperty(config.getKey()) : "null");
                configuration.addProperty(config.getKey(), config.getValue());
            }
        }

    }


    @Override
    public void onEvent(NameServerEvent event) {
        if (event.getMetaEvent() instanceof ConfigEvent) {
            ConfigEvent configEvent = (ConfigEvent) event.getMetaEvent();
            doUpdateProperty(new Config(configEvent.getGroup(), configEvent.getKey(), configEvent.getValue()));
        }
    }

    public interface ConfigProvider {
        /**
         * 获取所有的配置
         *
         * @return
         */
        List<Config> getConfigs();

        /**
         * 获取k-v配置
         *
         * @param group
         * @param key
         * @return
         */
        String getConfig(String group, String key);
    }
}
