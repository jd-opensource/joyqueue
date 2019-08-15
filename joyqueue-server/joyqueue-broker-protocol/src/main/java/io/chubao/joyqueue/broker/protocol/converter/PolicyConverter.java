package io.chubao.joyqueue.broker.protocol.converter;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.ConsumerPolicy;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.ProducerPolicy;

/**
 * PolicyConverter
 *
 * author: gaohaoxiang
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