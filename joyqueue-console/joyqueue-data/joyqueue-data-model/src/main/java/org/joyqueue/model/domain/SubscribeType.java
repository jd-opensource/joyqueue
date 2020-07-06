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
package org.joyqueue.model.domain;

import static org.joyqueue.model.domain.Consumer.CONSUMER_TYPE;
import static org.joyqueue.model.domain.Producer.PRODUCER_TYPE;

/**
 * 订阅类型
 */
public enum SubscribeType implements EnumItem {

    PRODUCER(PRODUCER_TYPE, "生产者"),
    CONSUMER(CONSUMER_TYPE, "消费者");

    private int value;
    private String description;

    SubscribeType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public int value() {
        return this.value;
    }

    @Override
    public String description() {
        return this.description;
    }

    public static SubscribeType resolve(Object valueOrName) {
        if (valueOrName == null) {
            return null;
        }
        for (SubscribeType type : SubscribeType.values()) {
            if ((valueOrName instanceof String && type.name().equals(valueOrName))
                    || (valueOrName instanceof Integer && type.value == Integer.valueOf(valueOrName.toString()))
                    || (valueOrName instanceof SubscribeType && type.name() == ((SubscribeType) valueOrName).name())) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return description;
    }
}
