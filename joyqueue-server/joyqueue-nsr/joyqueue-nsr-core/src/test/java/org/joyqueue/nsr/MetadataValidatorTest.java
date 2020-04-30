package org.joyqueue.nsr;

import com.google.common.collect.Lists;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.config.NameServiceConfigKey;
import org.joyqueue.nsr.nameservice.AllMetadataCache;
import org.joyqueue.nsr.nameservice.MetadataValidator;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MetadataValidatorTest
 * author: gaohaoxiang
 * date: 2020/3/30
 */
public class MetadataValidatorTest {

    private Map<String, Object> propertySupplierMap;
    private MetadataValidator metadataValidator;
    private AllMetadataCache allMetadata;

    @Before
    public void before() {
        propertySupplierMap = new HashMap<>();
        propertySupplierMap.put(NameServiceConfigKey.NAMESERVER_COMPENSATION_THRESHOLD.getName(), 10);
        metadataValidator = new MetadataValidator(new NameServiceConfig(new PropertySupplier.MapSupplier(propertySupplierMap)));
        initMetadata();
    }

    public void initMetadata() {
        List<TopicConfig> topics = Lists.newLinkedList();
        List<Consumer> consumers = Lists.newLinkedList();
        List<Producer> producers = Lists.newLinkedList();
        for (int i = 0; i < 100; i++) {
            TopicConfig topicConfig = TopicConfig.toTopicConfig(new Topic());
            topics.add(topicConfig);

            Consumer consumer = new Consumer();
            consumers.add(consumer);

            Producer producer = new Producer();
            producers.add(producer);
        }
        allMetadata = new AllMetadataCache();
        allMetadata.setAllTopicConfigs(topics);
        allMetadata.setAllConsumers(consumers);
        allMetadata.setAllProducers(producers);
    }

    @Test
    public void test() {
        AllMetadataCache newAllMetadata = new AllMetadataCache();
        newAllMetadata.setAllTopicConfigs(Lists.newArrayList(allMetadata.getAllTopicConfigs()));
        newAllMetadata.setAllConsumers(Lists.newArrayList(allMetadata.getAllConsumers()));
        newAllMetadata.setAllProducers(Lists.newArrayList(allMetadata.getAllProducers()));

        Assert.assertEquals(true, metadataValidator.validateChange(allMetadata, newAllMetadata));

        for (int i = 0; i < 11; i++) {
            newAllMetadata.getAllTopicConfigs().remove(0);
            newAllMetadata.getAllProducers().remove(0);
            newAllMetadata.getAllConsumers().remove(0);
        }

        Assert.assertEquals(false, metadataValidator.validateChange(allMetadata, newAllMetadata));

        for (int i = 0; i < 2; i++) {
            newAllMetadata.getAllTopicConfigs().add(newAllMetadata.getAllTopicConfigs().get(0));
            newAllMetadata.getAllProducers().add(newAllMetadata.getAllProducers().get(0));
            newAllMetadata.getAllConsumers().add(newAllMetadata.getAllConsumers().get(0));
        }

        Assert.assertEquals(true, metadataValidator.validateChange(allMetadata, newAllMetadata));
    }
}