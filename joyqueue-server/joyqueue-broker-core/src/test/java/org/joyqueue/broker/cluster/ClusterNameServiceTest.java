package org.joyqueue.broker.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joyqueue.broker.cluster.config.ClusterConfigKey;
import org.joyqueue.broker.cluster.entry.ClusterPartitionGroup;
import org.joyqueue.broker.cluster.entry.SplittedCluster;
import org.joyqueue.broker.cluster.helper.ClusterSplitHelper;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.store.StoreNode;
import org.joyqueue.store.StoreNodes;
import org.joyqueue.store.event.StoreNodeChangeEvent;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClusterNameServiceTest
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class ClusterNameServiceTest {

    private NameServiceStub nameServiceStub;
    private BrokerEventBusStub brokerEventBusStub;
    private PropertySupplier propertySupplier;
    private Map<String, Object> propertySupplierMap;
    private ClusterNameServiceStub clusterNameServiceStub;

    @Before
    public void before() throws Exception {
        this.nameServiceStub = new NameServiceStub();
        this.brokerEventBusStub = new BrokerEventBusStub(null);

        this.propertySupplierMap = new HashMap<>();
        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE.getName(), false);
        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_MIN_THREADS.getName(), 20);
        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_MAX_THREADS.getName(), 20);
        this.propertySupplier = new PropertySupplier.MapSupplier(propertySupplierMap);
        this.clusterNameServiceStub = new ClusterNameServiceStub(nameServiceStub, brokerEventBusStub, propertySupplier);

        this.nameServiceStub.start();
        this.brokerEventBusStub.start();
        this.clusterNameServiceStub.start();
        Broker broker = new Broker();
        broker.setId(1);
        this.clusterNameServiceStub.setBroker(broker);
        initTopics();
    }

    protected void initTopics() {
        Topic topic1 = new Topic();
        topic1.setName(TopicName.parse("test1"));
        topic1.setPartitions((short) 5);
        List<PartitionGroup> topic1PartitionGroups = Lists.newArrayList();
        PartitionGroup topic1PartitionGroup1 = new PartitionGroup();
        topic1PartitionGroup1.setTopic(topic1.getName());
        topic1PartitionGroup1.setGroup(0);
        topic1PartitionGroup1.setReplicas(Sets.newHashSet(-1));
        topic1PartitionGroup1.setLeader(-1);
        topic1PartitionGroup1.setPartitions(Sets.newHashSet((short) 0, (short) 1, (short) 2, (short) 3, (short) 4));
        this.nameServiceStub.addTopic(topic1, Arrays.asList(topic1PartitionGroup1));

        for (int i = 2; i < 10; i++) {
            Topic topic = new Topic();
            topic.setName(TopicName.parse("test" + i));
            List<PartitionGroup> partitionGroups = Lists.newArrayList();

            PartitionGroup partitionGroup1 = new PartitionGroup();
            partitionGroup1.setTopic(topic.getName());
            partitionGroup1.setGroup(0);
            partitionGroup1.setReplicas(Sets.newHashSet(1, 2, 3, 4, 5));
            partitionGroup1.setLeader(1);

            PartitionGroup partitionGroup2 = new PartitionGroup();
            partitionGroup2.setTopic(topic.getName());
            partitionGroup2.setGroup(1);
            partitionGroup2.setReplicas(Sets.newHashSet(2, 6, 7, 8, 9));
            partitionGroup2.setLeader(2);

            PartitionGroup partitionGroup3 = new PartitionGroup();
            partitionGroup3.setTopic(topic.getName());
            partitionGroup3.setGroup(2);
            partitionGroup3.setReplicas(Sets.newHashSet(3, 10, 11, 12, 13));
            partitionGroup3.setLeader(3);

            partitionGroups.add(partitionGroup1);
            partitionGroups.add(partitionGroup2);
            partitionGroups.add(partitionGroup3);
            this.nameServiceStub.addTopic(topic, partitionGroups);
        }
    }

    @After
    public void after() throws Exception {
        this.clusterNameServiceStub.stop();
        this.brokerEventBusStub.stop();
        this.nameServiceStub.stop();
    }

    @Test
    public void clusterNodeChangeTest() {
        Assert.assertEquals(null, clusterNameServiceStub.getTopicConfig(TopicName.parse("test0")));
        Assert.assertEquals(-1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());

        brokerEventBusStub.publishEvent(new StoreNodeChangeEvent("test1", 0, new StoreNodes(new StoreNode(1, false, true))));
        Assert.assertEquals(-1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());

        brokerEventBusStub.publishEvent(new StoreNodeChangeEvent("test1", 0, new StoreNodes(new StoreNode(1, true, true))));
        Assert.assertEquals(1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());

        propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_ENABLE.getName(), false);
        Assert.assertEquals(-1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());
        propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_ENABLE.getName(), true);

        PartitionGroup newPartitionGroup = clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).clone();
        newPartitionGroup.getReplicas().remove(1);
        nameServiceStub.addEvent(new NameServerEvent(new UpdatePartitionGroupEvent(TopicName.parse("test1"), clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0), newPartitionGroup), null));
        Assert.assertEquals(-1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());

        newPartitionGroup.getReplicas().add(1);
        nameServiceStub.addEvent(new NameServerEvent(new UpdatePartitionGroupEvent(TopicName.parse("test1"), clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0), newPartitionGroup), null));
        brokerEventBusStub.publishEvent(new StoreNodeChangeEvent("test1", 0, new StoreNodes(new StoreNode(1, true, true))));
        Assert.assertEquals(1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());

        nameServiceStub.addEvent(new NameServerEvent(new RemovePartitionGroupEvent(TopicName.parse("test1"), clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0)), null));
        Assert.assertEquals(-1, (int) clusterNameServiceStub.getTopicConfig(TopicName.parse("test1")).getPartitionGroups().get(0).getLeader());
    }

    @Test
    public void getTopicConfigCacheTest() {
        long total = clusterNameServiceStub.getTotal();
        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE.getName(), true);

        TopicConfig topicConfig = clusterNameServiceStub.getTopicConfig(TopicName.parse("test2"));
        Assert.assertEquals(total + 2, clusterNameServiceStub.getTotal());

        total = clusterNameServiceStub.getTotal();
        topicConfig = clusterNameServiceStub.getTopicConfig(TopicName.parse("test2"));
        Assert.assertEquals(total, clusterNameServiceStub.getTotal());

        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE.getName(), false);
    }

    @Test
    public void getTopicConfigsTest() {
        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE.getName(), true);
        clusterNameServiceStub.setSleep(1000 * 1);

        List<String> topics = Lists.newLinkedList();
        for (int i = 2; i < 10; i++) {
            topics.add("test" + i);
        }

        long startTime = SystemClock.now();
        Map<String, TopicConfig> topicConfigs = clusterNameServiceStub.getTopicConfigs(topics);
        Assert.assertEquals(true, SystemClock.now() - startTime >= 1000 * 2 && SystemClock.now() - startTime <= 1000 * 6);

        startTime = SystemClock.now();
        clusterNameServiceStub.getTopicConfigs(topics);
        Assert.assertEquals(true, SystemClock.now() - startTime < 1000 * 5);

        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE.getName(), false);
        this.propertySupplierMap.put(ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_PARALLEL_ENABLE.getName(), false);

        startTime = SystemClock.now();
        clusterNameServiceStub.getTopicConfigs(topics);
        Assert.assertEquals(true, SystemClock.now() - startTime >= 1000 * 2 * 8);

        clusterNameServiceStub.setSleep(0);
    }

    @Test
    public void getTopicConfigTest() {
        clusterNameServiceStub.setRemoteResult(false);
        TopicConfig topicConfig = clusterNameServiceStub.getTopicConfig(TopicName.parse("test2"));
        Assert.assertEquals(1, (int) topicConfig.getPartitionGroups().get(0).getLeader());
        Assert.assertEquals(2, (int) topicConfig.getPartitionGroups().get(1).getLeader());
        Assert.assertEquals(3, (int) topicConfig.getPartitionGroups().get(2).getLeader());
        Assert.assertEquals(Sets.newHashSet(1, 2, 3), clusterNameServiceStub.getLastRemote().get(0).keySet());
        Assert.assertEquals(Sets.newHashSet(4, 6, 8, 9, 10, 5, 7, 11, 12, 13), clusterNameServiceStub.getLastRemote().get(1).keySet());
        Assert.assertEquals(2, clusterNameServiceStub.getLastRemote().size());

        clusterNameServiceStub.setRemoteResult(true);
        clusterNameServiceStub.getLastRemote().clear();
        topicConfig = clusterNameServiceStub.getTopicConfig(TopicName.parse("test2"));
        Assert.assertEquals(1, (int) topicConfig.getPartitionGroups().get(0).getLeader());
        Assert.assertEquals(2, (int) topicConfig.getPartitionGroups().get(1).getLeader());
        Assert.assertEquals(3, (int) topicConfig.getPartitionGroups().get(2).getLeader());
        Assert.assertEquals(Sets.newHashSet(1, 2, 3), clusterNameServiceStub.getLastRemote().get(0).keySet());
        Assert.assertEquals(1, clusterNameServiceStub.getLastRemote().size());
    }

    @Test
    public void clusterSplitHelperTest() {
        Topic topic = new Topic();
        topic.setName(TopicName.parse("test1"));
        List<PartitionGroup> partitionGroups = Lists.newArrayList();

        ClusterPartitionGroup partitionGroup1 = new ClusterPartitionGroup();
        partitionGroup1.setTopic(topic.getName());
        partitionGroup1.setGroup(0);
        partitionGroup1.setReplicas(Sets.newHashSet(1, 2, 3, 4, 5));
        partitionGroup1.setLeader(1);

        ClusterPartitionGroup partitionGroup2 = new ClusterPartitionGroup();
        partitionGroup2.setTopic(topic.getName());
        partitionGroup2.setGroup(1);
        partitionGroup2.setReplicas(Sets.newHashSet(2, 6, 7, 8, 9));
        partitionGroup2.setLeader(2);

        ClusterPartitionGroup partitionGroup3 = new ClusterPartitionGroup();
        partitionGroup3.setTopic(topic.getName());
        partitionGroup3.setGroup(2);
        partitionGroup3.setReplicas(Sets.newHashSet(3, 10, 11, 12, 13));
        partitionGroup3.setLeader(3);

        partitionGroups.add(partitionGroup1);
        partitionGroups.add(partitionGroup2);
        partitionGroups.add(partitionGroup3);

        for (PartitionGroup topicPartitionGroup : partitionGroups) {
            topic.setPartitions((short) (topic.getPartitions() + topicPartitionGroup.getPartitions().size()));
        }

        TopicConfig topicConfig = TopicConfig.toTopicConfig(topic, partitionGroups);
        SplittedCluster split = ClusterSplitHelper.split(topicConfig, new ClusterNodeManager(null, null));

        Assert.assertEquals(Sets.newHashSet(1, 2, 3), split.getSplittedByLeader().keySet());
        Assert.assertEquals(Sets.newHashSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), split.getSplittedByGroup().keySet());

        for (Map.Entry<Integer, List<Integer>> entry : split.getSplittedByGroup().entrySet()) {
            List<Integer> all = Lists.newArrayList();
            for (PartitionGroup partitionGroup : partitionGroups) {
                if (partitionGroup.getReplicas().contains(entry.getKey())) {
                    all.add(partitionGroup.getGroup());
                }
            }
            Assert.assertEquals(entry.getValue(), all);
        }

        for (Map.Entry<Integer, List<Integer>> entry : split.getSplittedByLeader().entrySet()) {
            List<Integer> all = Lists.newArrayList();
            for (PartitionGroup partitionGroup : partitionGroups) {
                if (partitionGroup.getReplicas().contains(entry.getKey())) {
                    all.add(partitionGroup.getGroup());
                }
            }
            Assert.assertEquals(entry.getValue(), all);
        }

        partitionGroup1.setRewrite(true);

        Map<Integer, List<Integer>> splitByReWrite = ClusterSplitHelper.splitByReWrite(topicConfig);
        Assert.assertEquals(Sets.newHashSet(2, 3, 6, 7, 8, 9, 10, 11, 12, 13), splitByReWrite.keySet());
    }
}