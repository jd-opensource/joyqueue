package com.jd.journalq.broker.monitor.service;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface BrokerMonitorService extends BrokerMonitorInternalService, ConnectionMonitorService, ConsumerMonitorService, ProducerMonitorService,
        TopicMonitorService, PartitionMonitorService, CoordinatorMonitorService, ArchiveMonitorService, MetadataMonitorService {

}