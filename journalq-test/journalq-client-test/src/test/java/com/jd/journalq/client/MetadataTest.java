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
package com.jd.journalq.client;

import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.KeyValue;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.journalq.domain.JournalQNameServerBuiltinKeys;
import io.openmessaging.journalq.extension.QueueMetaDataAdapter;
import org.junit.Assert;
import org.junit.Test;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/13
 */
public class MetadataTest extends AbstractProducerTest {

    public static final String TOPIC = "test_topic_0";

    public static final String NEARBY_TOPIC = "test_nearby_topic";

    @Override
    protected KeyValue getAttributes() {
        KeyValue attributes = super.getAttributes();
        attributes.put(JournalQNameServerBuiltinKeys.METADATA_UPDATE_INTERVAL, 1000 * 5);
        return attributes;
    }

    @Test
    public void testMetadataUpdate() throws Exception {
        boolean changed = false;
        QueueMetaData lastQueueMetaData = producer.getQueueMetaData(TOPIC);

        for (int i = 0; i < 10; i++) {
            QueueMetaData queueMetaData = producer.getQueueMetaData(TOPIC);
            changed = !lastQueueMetaData.equals(queueMetaData);
            Thread.currentThread().sleep(1000 * 1);
        }

        Assert.assertEquals(true, changed);
    }

    @Test
    public void testNearby() {
        QueueMetaData queueMetaData = producer.getQueueMetaData(NEARBY_TOPIC);
        Assert.assertEquals(true, queueMetaData instanceof QueueMetaDataAdapter);

        TopicMetadata topicMetadata = ((QueueMetaDataAdapter) queueMetaData).getTopicMetadata();
        Assert.assertEquals(false, topicMetadata.getBrokers().isEmpty());
        Assert.assertEquals(false, topicMetadata.getNearbyBrokers().isEmpty());
    }

    @Test
    public void testNotNearby() {
        QueueMetaData queueMetaData = producer.getQueueMetaData(TOPIC);
        Assert.assertEquals(true, queueMetaData instanceof QueueMetaDataAdapter);

        TopicMetadata topicMetadata = ((QueueMetaDataAdapter) queueMetaData).getTopicMetadata();
        Assert.assertEquals(false, topicMetadata.getBrokers().isEmpty());
        Assert.assertEquals(false, topicMetadata.getNearbyBrokers().isEmpty());
    }
}