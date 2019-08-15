package io.chubao.joyqueue.broker.kafka;

/**
 * KafkaContextAware
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface KafkaContextAware {

    void setKafkaContext(KafkaContext kafkaContext);
}