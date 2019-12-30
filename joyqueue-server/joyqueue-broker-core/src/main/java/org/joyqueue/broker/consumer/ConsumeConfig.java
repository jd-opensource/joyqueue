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
package org.joyqueue.broker.consumer;

import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * @author chengzhiliang on 2018/10/22.
 */
public class ConsumeConfig {
    private static final String CONSUME_POSITION_PATH = "/position";
    private PropertySupplier propertySupplier;
    private String consumePositionPath;

    public ConsumeConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getConsumePositionPath() {

        if (consumePositionPath == null || consumePositionPath.isEmpty()) {
            synchronized (this) {
                if (consumePositionPath == null) {
                    String prefix = "";
                    if (propertySupplier != null) {
                        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
                        prefix = property == null ? prefix : property.getString();
                    }
                    consumePositionPath = prefix + CONSUME_POSITION_PATH;
                }

            }
        }
        return consumePositionPath;
    }

    public int getReplicateConsumePosInterval() {
        return PropertySupplier.getValue(propertySupplier, ConsumeConfigKey.REPLICATE_CONSUME_POS_INTERVAL);
    }
    public boolean getBroadcastIndexResetEnable() {
        return propertySupplier.getValue(ConsumeConfigKey.BROADCAST_INDEX_RESET_ENABLE);
    }

    public int getBroadcastIndexResetInterval() {
        return propertySupplier.getValue(ConsumeConfigKey.BROADCAST_INDEX_RESET_INTERVAL);
    }

    public int getBroadcastIndexResetTime() {
        return propertySupplier.getValue(ConsumeConfigKey.BROADCAST_INDEX_RESET_TIME);
    }

    public void setConsumePositionPath(String consumePositionPath) {
        this.consumePositionPath = consumePositionPath;
    }
}
