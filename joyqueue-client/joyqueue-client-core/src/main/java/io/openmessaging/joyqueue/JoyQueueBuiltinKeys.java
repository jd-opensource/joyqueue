package io.openmessaging.joyqueue;

import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueConsumerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueNameServerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueProducerBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueTransportBuiltinKeys;
import io.openmessaging.joyqueue.domain.JoyQueueTxFeedbackBuiltinKeys;

/**
 * JoyQueueBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JoyQueueBuiltinKeys extends OMSBuiltinKeys, JoyQueueNameServerBuiltinKeys, JoyQueueTransportBuiltinKeys,
        JoyQueueProducerBuiltinKeys, JoyQueueConsumerBuiltinKeys, JoyQueueTxFeedbackBuiltinKeys {

}