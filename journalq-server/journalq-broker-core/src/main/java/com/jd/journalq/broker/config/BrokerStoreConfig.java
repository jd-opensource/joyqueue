package com.jd.journalq.broker.config;

import com.jd.journalq.toolkit.config.PropertyDef;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * @author majun8
 */
public class BrokerStoreConfig {
    public final static String DEFAULT_CLEAN_STRATEGY_CLASS = "FixedSizeStoreCleaningStrategy";
    public final static long DEFAULT_MAX_STORE_SIZE = 10L * 1024 * 1024 * 1024;  // 10gb
    public final static long DEFAULT_MAX_STORE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7days
    public final static long DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN = 30 * 1000;
    public final static long DEFAULT_STORE_CLEAN_SCHEDULE_END = 60 * 1000;

    private PropertySupplier propertySupplier;

    public BrokerStoreConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    private enum BrokerStoreConfigKey implements PropertyDef {
        CLEAN_STRATEGY_CLASS("store.clean.strategy.class", DEFAULT_CLEAN_STRATEGY_CLASS, Type.STRING),
        MAX_STORE_SIZE("store.max.store.size", DEFAULT_MAX_STORE_SIZE, Type.LONG),
        MAX_STORE_TIME("store.max.store.time", DEFAULT_MAX_STORE_TIME, Type.LONG),
        CLEAN_SCHEDULE_BEGIN("store.clean.schedule.begin", DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN, Type.LONG),
        CLEAN_SCHEDULE_END("store.clean.schedule.end", DEFAULT_STORE_CLEAN_SCHEDULE_END, Type.LONG);

        private String name;
        private Object value;
        private PropertyDef.Type type;

        BrokerStoreConfigKey(String name, Object value, PropertyDef.Type type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Type getType() {
            return type;
        }
    }

    public String getCleanStrategyClass() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.CLEAN_STRATEGY_CLASS, DEFAULT_CLEAN_STRATEGY_CLASS);
    }

    public long getMaxStoreSize() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.MAX_STORE_SIZE, DEFAULT_MAX_STORE_SIZE);
    }

    public long getMaxStoreTime() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.MAX_STORE_TIME, DEFAULT_MAX_STORE_TIME);
    }

    public long getStoreCleanScheduleBegin() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.CLEAN_SCHEDULE_BEGIN, DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN);
    }

    public long getStoreCleanScheduleEnd() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.CLEAN_SCHEDULE_END, DEFAULT_STORE_CLEAN_SCHEDULE_END);
    }
}
