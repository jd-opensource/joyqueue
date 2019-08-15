package io.chubao.joyqueue.broker.kafka.manage.support;

import io.chubao.joyqueue.broker.kafka.manage.KafkaGroupManageService;
import io.chubao.joyqueue.broker.kafka.manage.KafkaManageService;

/**
 * KafkaManageService
 *
 * author: gaohaoxiang
 * date: 2018/11/13
 */
public class DefaultKafkaManageService implements KafkaManageService {

    private KafkaGroupManageService kafkaGroupManageService;

    public DefaultKafkaManageService(KafkaGroupManageService kafkaGroupManageService) {
        this.kafkaGroupManageService = kafkaGroupManageService;
    }

    @Override
    public boolean removeGroup(String groupId) {
        return kafkaGroupManageService.removeGroup(groupId);
    }

    @Override
    public boolean rebalanceGroup(String groupId) {
        return kafkaGroupManageService.rebalanceGroup(groupId);
    }
}