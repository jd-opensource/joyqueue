package com.jd.journalq.broker.config;

import com.jd.journalq.toolkit.config.PropertyDef;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * @author majun8
 */
public class BrokerStoreConfig {
    public static final String path = "store.path";
    public final static long DEFAULT_MAX_STORE_SIZE = 10L * 1024 * 1024 * 1024;  // 10gb
    public final static long DEFAULT_MAX_STORE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7days

    private PropertySupplier propertySupplier;

    public BrokerStoreConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    private enum BrokerStoreConfigKey implements PropertyDef {
        MAX_STORE_SIZE("store.max.store.size", DEFAULT_MAX_STORE_SIZE, Type.LONG),
        MAX_STORE_TIME("store.max.store.time", DEFAULT_MAX_STORE_TIME, Type.LONG);

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

    public long getMaxStoreSize() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.MAX_STORE_SIZE, DEFAULT_MAX_STORE_SIZE);
    }

    public long getMaxStoreTime() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.MAX_STORE_TIME, DEFAULT_MAX_STORE_TIME);
    }
}
