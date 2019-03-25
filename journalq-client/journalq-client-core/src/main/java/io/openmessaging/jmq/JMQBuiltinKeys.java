package io.openmessaging.jmq;

import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.jmq.domain.JMQConsumerBuiltinKeys;
import io.openmessaging.jmq.domain.JMQNameServerBuiltinKeys;
import io.openmessaging.jmq.domain.JMQProducerBuiltinKeys;
import io.openmessaging.jmq.domain.JMQTransportBuiltinKeys;
import io.openmessaging.jmq.domain.JMQTxFeedbackBuiltinKeys;

/**
 * JMQBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JMQBuiltinKeys extends OMSBuiltinKeys, JMQNameServerBuiltinKeys, JMQTransportBuiltinKeys, JMQProducerBuiltinKeys, JMQConsumerBuiltinKeys, JMQTxFeedbackBuiltinKeys {

}