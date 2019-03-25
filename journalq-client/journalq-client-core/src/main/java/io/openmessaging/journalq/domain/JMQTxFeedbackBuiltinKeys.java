package io.openmessaging.journalq.domain;

import io.openmessaging.OMSBuiltinKeys;

/**
 * JMQTxFeedbackBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JMQTxFeedbackBuiltinKeys extends OMSBuiltinKeys {

    String TIMEOUT = "TX_FEEDBACK_TIMEOUT";

    String LONGPOLL_TIMEOUT = "TX_FEEDBACK_LONGPOLL_TIMEOUT";

    String FETCH_INTERVAL = "TX_FEEDBACK_FETCH_INTERVAL";

    String FETCH_SIZE = "TX_FEEDBACK_FETCH_SIZE";
}