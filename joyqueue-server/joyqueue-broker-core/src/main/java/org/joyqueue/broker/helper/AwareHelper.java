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
package org.joyqueue.broker.helper;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.toolkit.config.PropertySupplierAware;

import java.util.List;

/**
 * AwareHelper
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public class AwareHelper {

    public static <T> List<T> enrichIfNecessary(List<T> list, BrokerContext brokerContext) {
        if (list == null) {
            return list;
        }
        for (T obj : list) {
            enrichIfNecessary(obj, brokerContext);
        }
        return list;
    }

    public static <T> T enrichIfNecessary(T obj, BrokerContext brokerContext) {
        if (obj == null) {
            return obj;
        }
        if (obj instanceof PropertySupplierAware) {
            ((PropertySupplierAware) obj).setSupplier(brokerContext.getPropertySupplier());
        }

        if (obj instanceof BrokerContextAware) {
            ((BrokerContextAware) obj).setBrokerContext(brokerContext);
        }
        return obj;
    }
}