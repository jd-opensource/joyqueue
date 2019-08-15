package io.chubao.joyqueue.broker.kafka.manage;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.manage.support.DefaultKafkaGroupManageService;
import io.chubao.joyqueue.broker.kafka.manage.support.DefaultKafkaManageService;
import io.chubao.joyqueue.broker.kafka.manage.support.DefaultKafkaMonitorService;

/**
 * KafkaManageServiceFactory
 *
 * author: gaohaoxiang
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
        DefaultKafkaGroupManageService kafkaGroupManageService = new DefaultKafkaGroupManageService(kafkaContext.getGroupCoordinator());
        return new DefaultKafkaManageService(kafkaGroupManageService);
    }

    protected KafkaMonitorService newKafkaMonitorService(BrokerContext brokerContext, KafkaContext kafkaContext) {
        return new DefaultKafkaMonitorService();
    }
}