package com.jd.journalq.broker.kafka.config;

import com.jd.journalq.toolkit.config.PropertyDef;
import com.jd.journalq.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class KafkaConfig {
    protected static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    private PropertySupplier propertySupplier;

    public KafkaConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public boolean isRateLimit(int type) {
        return getConfig(KafkaConfigKey.RATE_LIMIT_ENABLE.getName() + "." + type, PropertyDef.Type.BOOLEAN, KafkaConfigKey.RATE_LIMIT_ENABLE.getValue());
    }

    public int getRateLimitDelay(int type) {
        return getConfig(KafkaConfigKey.RATE_LIMIT_DELAY.getName() + "." + type, PropertyDef.Type.INT, KafkaConfigKey.RATE_LIMIT_DELAY.getValue());
    }

    public int getRateLimitTimes(int type) {
        return getConfig(KafkaConfigKey.RATE_LIMIT_TIMES.getName() + "." + type, PropertyDef.Type.INT, KafkaConfigKey.RATE_LIMIT_TIMES.getValue());
    }

    public boolean isRateLimitEnable() {
        return getConfig(KafkaConfigKey.RATE_LIMIT_ENABLE);
    }

    public int getRateLimitWindowSize() {
        return getConfig(KafkaConfigKey.RATE_LIMIT_TIME_WINDOW_SIZE);
    }

    public int getRateLimitDelay() {
        return getConfig(KafkaConfigKey.RATE_LIMIT_DELAY);
    }

    public int getRateLimitTimes() {
        return getConfig(KafkaConfigKey.RATE_LIMIT_TIMES);
    }

    public int getFetchBatchSize() {
        return getConfig(KafkaConfigKey.FETCH_BATCH_SIZE);
    }

    public int getCoordinatorOffsetSessionCache() {
        return getConfig(KafkaConfigKey.COORDINATOR_OFFSET_SESSION_CACHE);
    }

    public int getCoordinatorOffsetSyncTimeout() {
        return getConfig(KafkaConfigKey.COORDINATOR_OFFSET_SYNC_TIMEOUT);
    }

    public int getSessionMaxTimeout() {
        return getConfig(KafkaConfigKey.SESSION_MIN_TIMEOUT);
    }

    public int getSessionMinTimeout() {
        return getConfig(KafkaConfigKey.SESSION_MAX_TIMEOUT);
    }

    public int getPermitsPerSecond() {
        return getConfig(KafkaConfigKey.PERMITSPER_SECOND);
    }

    public int getAcquirePermitsTimeout() {
        return getConfig(KafkaConfigKey.ACQUIRE_PERMITS_TIMEOUT);
    }

    public int getRebalanceInitialDelay() {
        return getConfig(KafkaConfigKey.REBALANCE_INITIAL_DELAY);
    }

    public int getRebalanceTimeout() {
        return getConfig(KafkaConfigKey.REBALANCE_TIMEOUT);
    }

    protected <T> T getConfig(String key, PropertyDef.Type type, Object defaultValue) {
        return PropertySupplier.getValue(this.propertySupplier, key, type, defaultValue);
    }

    protected <T> T getConfig(PropertyDef key) {
        return PropertySupplier.getValue(this.propertySupplier, key);
    }
}