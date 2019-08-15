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
package io.chubao.joyqueue.broker;

import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.consumer.MessageConverter;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.limit.LimitRejectedStrategy;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.security.Authentication;
import io.chubao.joyqueue.server.archive.store.api.ArchiveStore;
import io.chubao.joyqueue.server.retry.api.MessageRetry;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;

public interface Plugins {
    /**
     * 消费扩展点
     */
    ExtensionPoint<Consume, String> CONSUME = new ExtensionPointLazy<>(Consume.class, SpiLoader.INSTANCE, null, null);
    /**
     * 发送扩展点
     */
    ExtensionPoint<Produce, String> PRODUCE = new ExtensionPointLazy<>(Produce.class, SpiLoader.INSTANCE, null, null);
    /**
     * 命名服务扩展点
     */
    ExtensionPoint<NameService, String> NAMESERVICE = new ExtensionPointLazy<>(NameService.class, SpiLoader.INSTANCE, null, null);

    /**
     * 存储扩展点
     */
    ExtensionPoint<StoreService, String> STORE = new ExtensionPointLazy<>(StoreService.class, SpiLoader.INSTANCE, null, null);

    /**
     * 选举扩展点
     */
    ExtensionPoint<ElectionService, String> ELECTION = new ExtensionPointLazy<>(ElectionService.class, SpiLoader.INSTANCE, null, null);

    /**
     * 认证扩展点
     */
    ExtensionPoint<Authentication, String> AUTHENTICATION = new ExtensionPointLazy<>(Authentication.class, SpiLoader.INSTANCE, null, null);

    /**
     * 重试扩展点
     */
    ExtensionPoint<MessageRetry, String> MESSAGERETRY = new ExtensionPointLazy<>(MessageRetry.class, SpiLoader.INSTANCE, null, null);
    /**
     * 归档扩展点
     */
    ExtensionPoint<ArchiveStore, String> ARCHIVESTORE = new ExtensionPointLazy<>(ArchiveStore.class, SpiLoader.INSTANCE, null, null);
    /**
     * 配置文件扩展点
     */
    ExtensionPoint<PropertySupplier, String> PROPERTY = new ExtensionPointLazy<>(PropertySupplier.class, SpiLoader.INSTANCE, null, null);

    /**
     * 消息转换扩展点
     */
    ExtensionPoint<MessageConverter, String> MESSAGE_CONVERTER = new ExtensionPointLazy<>(MessageConverter.class, SpiLoader.INSTANCE, null, null);

    /**
     * 限流拒绝策略扩展点
     */
    ExtensionPoint<LimitRejectedStrategy, String> LIMIT_REJECTED_STRATEGY = new ExtensionPointLazy<>(LimitRejectedStrategy.class, SpiLoader.INSTANCE, null, null);


}
