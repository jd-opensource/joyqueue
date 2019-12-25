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
package org.joyqueue.client.internal.producer.interceptor;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.common.ordered.OrderedSorter;

import java.util.List;

/**
 * ProducerInterceptorManager
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class ProducerInterceptorManager {

    private List<ProducerInterceptor> interceptors = Lists.newLinkedList();
    private List<ProducerInterceptor> sortedInterceptors;

    public List<ProducerInterceptor> getSortedInterceptors() {
        if (sortedInterceptors != null) {
            return sortedInterceptors;
        }
        List<ProducerInterceptor> sortedInterceptors = Lists.newLinkedList();
        sortedInterceptors.addAll(GlobalProducerInterceptorManager.getInterceptors());
        sortedInterceptors.addAll(interceptors);
        OrderedSorter.sort(sortedInterceptors);
        this.sortedInterceptors = sortedInterceptors;
        return sortedInterceptors;
    }

    public void addInterceptor(ProducerInterceptor interceptor) {
        interceptors.add(interceptor);
        sortedInterceptors = null;
    }

    public void removeInterceptor(ProducerInterceptor interceptor) {
        interceptors.remove(interceptor);
        sortedInterceptors = null;
    }
}