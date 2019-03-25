package io.openmessaging.journalq;

import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQConsumerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQNameServerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQProducerBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQTransportBuiltinKeys;
import io.openmessaging.journalq.domain.JournalQTxFeedbackBuiltinKeys;

/**
 * JournalQBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JournalQBuiltinKeys extends OMSBuiltinKeys, JournalQNameServerBuiltinKeys, JournalQTransportBuiltinKeys,
        JournalQProducerBuiltinKeys, JournalQConsumerBuiltinKeys, JournalQTxFeedbackBuiltinKeys {

}