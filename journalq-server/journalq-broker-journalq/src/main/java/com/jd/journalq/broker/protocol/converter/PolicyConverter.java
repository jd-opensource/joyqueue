package com.jd.journalq.broker.protocol.converter;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.ConsumerPolicy;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.ProducerPolicy;

/**
 * PolicyConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/26
 */
public class PolicyConverter {

    public static ProducerPolicy convertProducer(Producer.ProducerPolicy policy) {
        return new ProducerPolicy(policy.getNearby(), policy.isSingle(), policy.getArchive(),
                policy.getWeight(), policy.getBlackList(), policy.getTimeOut());
    }

    public static ConsumerPolicy convertConsumer(Consumer.ConsumerPolicy policy) {
        return new ConsumerPolicy(policy.getNearby(), policy.getPaused(), policy.getArchive(), policy.getRetry(),
                policy.getSeq(), policy.getAckTimeout(), policy.getBatchSize(), policy.getConcurrent(), policy.getDelay(),
                policy.getBlackList(), policy.getErrTimes(), policy.getMaxPartitionNum(), policy.getReadRetryProbability(),
                policy.getFilters());
    }
}