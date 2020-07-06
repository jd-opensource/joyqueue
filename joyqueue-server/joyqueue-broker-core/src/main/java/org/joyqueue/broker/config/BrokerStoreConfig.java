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
package org.joyqueue.broker.config;

import org.joyqueue.toolkit.config.PropertyDef;
import org.joyqueue.toolkit.config.PropertySupplier;
import static org.joyqueue.toolkit.config.Property.APPLICATION_DATA_PATH;

/**
 * @author majun8
 */
public class BrokerStoreConfig {
    public static final String DEFAULT_CLEAN_STRATEGY_CLASS = "GlobalStorageLimitCleaningStrategy";
    public static final long DEFAULT_MAX_STORE_SIZE = 10L * 1024 * 1024 * 1024;  // 10gb
    public static final long DEFAULT_MAX_STORE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7days
    public static final long DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN = 5 * 60 * 1000;
    public static final long DEFAULT_STORE_CLEAN_SCHEDULE_END = 10 * 60 * 1000;
    public static final boolean DEFAULT_KEEP_UNCONSUMED = true;
    public static final int DEFAULT_STORE_DISK_USAGE_MAX= 80;
    public static final int DEFAULT_STORE_DISK_USAGE_SAFE=75;
    private PropertySupplier propertySupplier;


    public BrokerStoreConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public enum BrokerStoreConfigKey implements PropertyDef {
        CLEAN_STRATEGY_CLASS("store.clean.strategy.class", DEFAULT_CLEAN_STRATEGY_CLASS, Type.STRING),
        MAX_STORE_SIZE("store.max.store.size", DEFAULT_MAX_STORE_SIZE, Type.LONG),
        MAX_STORE_TIME("store.max.store.time", DEFAULT_MAX_STORE_TIME, Type.LONG),
        MAX_STORE_TIME_TOPIC_PREFIX("store.max.store.time.", -1, Type.LONG),
        KEEP_UNCONSUMED("store.clean.keep.unconsumed", DEFAULT_KEEP_UNCONSUMED, Type.BOOLEAN),
        KEEP_UNCONSUMED_TOPIC_PREFIX("store.clean.keep.unconsumed.",null, Type.STRING),
        CLEAN_SCHEDULE_BEGIN("store.clean.schedule.begin", DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN, Type.LONG),
        CLEAN_SCHEDULE_END("store.clean.schedule.end", DEFAULT_STORE_CLEAN_SCHEDULE_END, Type.LONG),
        FORCE_RESTORE("store.force.restore", true, Type.BOOLEAN),
        STORE_DISK_USAGE_MAX("store.disk.usage.max",DEFAULT_STORE_DISK_USAGE_MAX,Type.INT),
        STORE_DISK_USAGE_SAFE("store.disk.usage.safe",DEFAULT_STORE_DISK_USAGE_SAFE,Type.INT);
        private String name;
        private Object value;
        private Type type;

        BrokerStoreConfigKey(String name, Object value, Type type) {
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

    /**
     * Topic max store time
     *
     * @return  config order
     *   1. topic  level
     *   2. broker level
     *   3. broker level default
     *
     **/
    public long getMaxStoreTime(String topic){
        long storeTime= PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.MAX_STORE_TIME_TOPIC_PREFIX.getName()+topic,
                                                  BrokerStoreConfigKey.MAX_STORE_TIME_TOPIC_PREFIX.getType(), BrokerStoreConfigKey.MAX_STORE_TIME_TOPIC_PREFIX.getValue());

        return storeTime<0?getMaxStoreTime():storeTime;
    }

    /**
     * Topic keep unconsumed message option
     * @return  config order
     *   1. topic  level
     *   2. broker level
     *   3. broker level default
     *
     * */
    public boolean keepUnconsumed(String topic){
        String keepUnconsumed= PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.KEEP_UNCONSUMED_TOPIC_PREFIX.getName()+topic,
                BrokerStoreConfigKey.KEEP_UNCONSUMED_TOPIC_PREFIX.getType(), BrokerStoreConfigKey.KEEP_UNCONSUMED_TOPIC_PREFIX.getValue());
        return keepUnconsumed==null?keepUnconsumed():Boolean.valueOf(keepUnconsumed);
    }


    public boolean keepUnconsumed() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.KEEP_UNCONSUMED, DEFAULT_KEEP_UNCONSUMED);
    }

    public long getStoreCleanScheduleBegin() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.CLEAN_SCHEDULE_BEGIN, DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN);
    }

    public long getStoreCleanScheduleEnd() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.CLEAN_SCHEDULE_END, DEFAULT_STORE_CLEAN_SCHEDULE_END);
    }

    public boolean getForceRestore() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.FORCE_RESTORE);
    }

    /**
     * Once wal storage reach force clean threshold, at least clean storage fraction
     **/
    public int getStoreDiskUsageMax(){
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.STORE_DISK_USAGE_MAX);
    }
    /**
     *
     *  Start to stop force clean consumed log threshold
     *
     **/
    public int getStoreDiskUsageSafe(){
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.STORE_DISK_USAGE_SAFE);
    }

    /**
     *  Application Data path
     *  @return application data path
     *  s
     **/
    public String getApplicationDataPath(){
        return propertySupplier.getOrCreateProperty(APPLICATION_DATA_PATH).getString();
    }
}

