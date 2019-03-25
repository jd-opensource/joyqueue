package com.jd.journalq.broker.monitor.config;

import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.toolkit.config.PropertySupplier;


/**
 * broker监控配置
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    public int getWriterSize() {
        return propertySupplier.getValue(BrokerMonitorConfigKey.WRITER_SIZE);
    }

    public int getWriterThread() {
        return propertySupplier.getValue(BrokerMonitorConfigKey.WRITER_THREAD);
    }

    public String getStatSaveFile() {
        return brokerConfig.getAndCreateDataPath() + propertySupplier.getValue(BrokerMonitorConfigKey.STAT_SAVE_FILE);
    }

    public int getStatSaveInterval() {
        return propertySupplier.getValue(BrokerMonitorConfigKey.STAT_SAVE_INTERVAL);
    }

}