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
package io.chubao.joyqueue.broker.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

/**
 * @author majun8
 */
public class BrokerStoreConfig {
    public static final String DEFAULT_CLEAN_STRATEGY_CLASS = "FixedSizeStoreCleaningStrategy";
    public static final long DEFAULT_MAX_STORE_SIZE = 10L * 1024 * 1024 * 1024;  // 10gb
    public static final long DEFAULT_MAX_STORE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7days
    public static final long DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN = 5 * 60 * 1000;
    public static final long DEFAULT_STORE_CLEAN_SCHEDULE_END = 10 * 60 * 1000;
    public static final boolean DEFAULT_DO_NOT_DELETE_CONSUMED = true;

    private PropertySupplier propertySupplier;

    public BrokerStoreConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    private enum BrokerStoreConfigKey implements PropertyDef {
        CLEAN_STRATEGY_CLASS("store.clean.strategy.class", DEFAULT_CLEAN_STRATEGY_CLASS, Type.STRING),
        MAX_STORE_SIZE("store.max.store.size", DEFAULT_MAX_STORE_SIZE, Type.LONG),
        MAX_STORE_TIME("store.max.store.time", DEFAULT_MAX_STORE_TIME, Type.LONG),
        DO_NOT_DELETE_CONSUMED("store.clean.donot.delete.consumed", DEFAULT_DO_NOT_DELETE_CONSUMED, Type.BOOLEAN),
        CLEAN_SCHEDULE_BEGIN("store.clean.schedule.begin", DEFAULT_STORE_CLEAN_SCHEDULE_BEGIN, Type.LONG),
        CLEAN_SCHEDULE_END("store.clean.schedule.end", DEFAULT_STORE_CLEAN_SCHEDULE_END, Type.LONG),
        FORCE_RESTORE("store.force.restore", false, Type.BOOLEAN);

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

    public boolean getDoNotDeleteConsumed() {
        return PropertySupplier.getValue(propertySupplier, BrokerStoreConfigKey.DO_NOT_DELETE_CONSUMED, DEFAULT_DO_NOT_DELETE_CONSUMED);
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
}
