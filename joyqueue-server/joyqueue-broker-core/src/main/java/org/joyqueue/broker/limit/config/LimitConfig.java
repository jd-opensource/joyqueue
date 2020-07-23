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
package org.joyqueue.broker.limit.config;

import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * LimitConfig
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class LimitConfig {

    public static final int DELAY_DYNAMIC = -1;

    private PropertySupplier propertySupplier;

    public LimitConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public boolean isEnable() {
        return propertySupplier.getValue(LimitConfigKey.ENABLE);
    }

    public int getDelay() {
        return propertySupplier.getValue(LimitConfigKey.DELAY);
    }

    public int getMaxDelay() {
        return propertySupplier.getValue(LimitConfigKey.MAX_DELAY);
    }

    public int getMinDelay() {
        return propertySupplier.getValue(LimitConfigKey.MIN_DELAY);
    }

    public String getRejectedStrategy() {
        return propertySupplier.getValue(LimitConfigKey.REJECTED_STRATEGY);
    }
}