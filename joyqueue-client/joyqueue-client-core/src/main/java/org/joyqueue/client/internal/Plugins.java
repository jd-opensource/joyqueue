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
package org.joyqueue.client.internal;

import org.joyqueue.client.internal.common.compress.Compressor;
import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import org.joyqueue.client.internal.consumer.converter.MessageConverter;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import org.joyqueue.client.internal.producer.PartitionSelector;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import org.joyqueue.client.internal.trace.Trace;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;

public interface Plugins {
    /**
     * 压缩插件
     */
    ExtensionPoint<Compressor, String> COMPRESSORS = new ExtensionPointLazy<>(Compressor.class);
    /**
     * 发送过拦截器插件
     */
    ExtensionPoint<ProducerInterceptor, String> PRODUCER_INTERCEPTOR = new ExtensionPointLazy<>(ProducerInterceptor.class);
    /**
     * 消费拦截器插件
     */
    ExtensionPoint<ConsumerInterceptor, String> CONSUMER_INTERCEPTOR = new ExtensionPointLazy<>(ConsumerInterceptor.class);
    /**
     * 负载均衡插件
     */
    ExtensionPoint<BrokerLoadBalance, String> LOADBALANCE = new ExtensionPointLazy<>(BrokerLoadBalance.class);
    /**
     * partition 选择器
     */
    ExtensionPoint<PartitionSelector, String> PARTITION_SELECTOR = new ExtensionPointLazy<>(PartitionSelector.class);
    /**
     * trace 插件
     */
    ExtensionPoint<Trace, String> TRACE = new ExtensionPointLazy<>(Trace.class);

    /**
     * 消息转换扩展点
     */
    ExtensionPoint<MessageConverter, String> MESSAGE_CONVERTER = new ExtensionPointLazy<>(MessageConverter.class, SpiLoader.INSTANCE, null, null);
}
