package com.jd.journalq.broker.kafka.manage;

import com.jd.journalq.broker.kafka.manage.support.DefaultKafkaGroupManageService;
import com.jd.journalq.broker.kafka.manage.support.DefaultKafkaManageService;
import com.jd.journalq.broker.kafka.manage.support.DefaultKafkaMonitorService;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.kafka.KafkaContext;

/**
 * KafkaManageServiceFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public class KafkaManageServiceFactory {

    private KafkaManageService kafkaManageService;
    private KafkaMonitorService kafkaMonitorService;

    public KafkaManageServiceFactory(BrokerContext brokerContext, KafkaContext kafkaContext) {
        this.kafkaManageService = newKafkaManageService(brokerContext, kafkaContext);
        this.kafkaMonitorService = newKafkaMonitorService(brokerContext, kafkaContext);
    }

    public KafkaManageService getKafkaManageService() {
        return kafkaManageService;
    }

    public KafkaMonitorService getKafkaMonitorService() {
        return kafkaMonitorService;
    }

    protected KafkaManageService newKafkaManageService(BrokerContext brokerContext, KafkaContext kafkaContext) {
        DefaultKafkaGroupManageService kafkaGroupManageService = new DefaultKafkaGroupManageService(kafkaContext.getGroupMetadataManager());
        return new DefaultKafkaManageService(kafkaGroupManageService);
    }

    protected KafkaMonitorService newKafkaMonitorService(BrokerContext brokerContext, KafkaContext kafkaContext) {
        return new DefaultKafkaMonitorService();
    }
}