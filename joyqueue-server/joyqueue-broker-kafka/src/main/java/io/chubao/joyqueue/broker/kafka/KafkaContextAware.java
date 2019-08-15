package io.chubao.joyqueue.broker.kafka;

/**
 * KafkaContextAware
 *
 * author: gaohaoxiang
 * date: 2019/2/28
 */
public interface KafkaContextAware {

    void setKafkaContext(KafkaContext kafkaContext);
}