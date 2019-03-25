package io.openmessaging.journalq;

import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.journalq.domain.JMQConsumerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQNameServerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQProducerBuiltinKeys;
import io.openmessaging.journalq.domain.JMQTransportBuiltinKeys;
import io.openmessaging.journalq.domain.JMQTxFeedbackBuiltinKeys;

/**
 * JMQBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JMQBuiltinKeys extends OMSBuiltinKeys, JMQNameServerBuiltinKeys, JMQTransportBuiltinKeys, JMQProducerBuiltinKeys, JMQConsumerBuiltinKeys, JMQTxFeedbackBuiltinKeys {

}