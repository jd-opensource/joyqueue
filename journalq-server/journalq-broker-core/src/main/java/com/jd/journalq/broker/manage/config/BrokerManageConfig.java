package com.jd.journalq.broker.manage.config;

import com.jd.journalq.broker.config.BrokerConfig;
import com.jd.journalq.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * broker监控配置
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
        return PropertySupplier.getValue(propertySupplier,BrokerManageConfigKey.EXPORT_PORT,brokerConfig.getBroker().getMonitorPort());
    }

    public String getExportHost() {
        return PropertySupplier.getValue(propertySupplier,BrokerManageConfigKey.EXPORT_HOST);
    }
}