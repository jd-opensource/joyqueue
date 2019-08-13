/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chubao.joyqueue.broker.protocol.converter;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.ConsumerPolicy;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.ProducerPolicy;

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