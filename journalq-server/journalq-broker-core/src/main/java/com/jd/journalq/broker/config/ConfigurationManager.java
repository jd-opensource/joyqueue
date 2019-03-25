package com.jd.journalq.broker.config;

import com.jd.journalq.domain.Config;
import com.jd.journalq.event.ConfigEvent;
import com.jd.journalq.event.EventType;
import com.jd.journalq.event.NameServerEvent;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 上下文管理器
 */
public class ConfigurationManager extends Service implements EventListener<NameServerEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private ConfigProvider configProvider;
    // 事件派发器
    private EventBus<ConfigEvent> eventManager = new EventBus<ConfigEvent>("ContextManager");

    private Configuration configuration;

    public ConfigurationManager(Configuration configuration) {
        this.configuration = configuration;
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (!eventManager.isStarted()) eventManager.start();
        //加载ignite中配置
        updateConfig();
        logger.info("context manager is started");
    }

    @Override
    protected void doStop() {
        super.doStop();
        eventManager.stop();
        logger.info("context manager is stopped");
    }

    /**
     * 添加监听器
     *
     * @param listener 监听器
     */
    public void addListener(EventListener<ConfigEvent> listener) {
        if (eventManager.addListener(listener)) {
            if (isStarted()) updateConfig();
        }
        ;
    }

    private void updateConfig() {
        List<Config> configs = configProvider.getConfigs();
        if (null != configs) for (Config config : configs) {
            eventManager.add(new ConfigEvent(EventType.ADD_CONFIG, config.getGroup(), config.getKey(), config.getValue()));
        }
    }

    /**
     * 移除监听器
     *
     * @param listener 监听器
     */
    public void removeListener(EventListener<ConfigEvent> listener) {
        eventManager.removeListener(listener);
    }

    @Override
    public void onEvent(NameServerEvent event) {
        if (event.getMetaEvent() instanceof ConfigEvent) {
            ConfigEvent configEvent = (ConfigEvent) event.getMetaEvent();
            eventManager.add(new ConfigEvent(configEvent.getEventType(), configEvent.getGroup(), configEvent.getKey(), configProvider.getConfig(configEvent.getGroup(), configEvent.getKey())));
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
