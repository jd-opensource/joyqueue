package io.openmessaging.journalq.domain;

import io.openmessaging.OMSBuiltinKeys;

/**
 * JournalQProducerBuiltinKeys
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public interface JournalQProducerBuiltinKeys extends OMSBuiltinKeys {

    String TIMEOUT = "PRODUCER_TIMEOUT";

    String PRODUCE_TIMEOUT = "PRODUCER_PRODUCE_TIMEOUT";

    String TRANSACTION_TIMEOUT = "PRODUCER_TRANSACTION_TIMEOUT";

    String FAILOVER = "PRODUCER_FAILOVER";

    String RETRIES = "PRODUCER_RETRIES";

    String QOSLEVEL = "PRODUCER_QOSLEVEL";

    String COMPRESS = "PRODUCER_COMPRESS";

    String COMPRESS_TYPE = "PRODUCER_COMPRESS_TYPE";

    String COMPRESS_THRESHOLD = "PRODUCER_COMPRESS_THRESHOLD";

    String BATCH = "PRODUCER_BATCH";

    String SELECTOR_TYPE = "PRODUCER_SELECTOR_TYPE";

    String BUSINESSID_LENGTH_LIMIT = "PRODUCER_BUSINESSID_LENGTH_LIMIT";

    String BODY_LENGTH_LIMIT = "PRODUCER_BODY_LENGTH_LIMIT";

    String BATCH_BODY_LENGTH_LIMIT = "PRODUCER_BATCH_BODY_LENGTH_LIMIT";

}