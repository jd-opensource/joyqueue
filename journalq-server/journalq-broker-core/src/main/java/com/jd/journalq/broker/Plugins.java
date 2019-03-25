package com.jd.journalq.broker;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.common.security.Authentication;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.server.archive.store.api.ArchiveStore;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.PropertySupplier;
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


}
