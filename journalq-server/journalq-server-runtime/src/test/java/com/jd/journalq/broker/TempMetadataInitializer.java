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
package com.jd.journalq.broker;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.domain.AppToken;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.domain.TopicType;
import com.jd.journalq.network.transport.config.ServerConfig;
import com.jd.journalq.nsr.MetaManager;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.nsr.service.AppTokenService;
import com.jd.journalq.nsr.service.ConsumerService;
import com.jd.journalq.nsr.service.ProducerService;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 临时元数据初始化器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/13
 */
public class TempMetadataInitializer extends Service {

    public static final String TOPIC = "test_topic";

    public static final String NAMESPACE = "";

    public static final int TOPIC_COUNT = 5;

    public static final short PARTITION_GROUPS = 10;

    public static final short PARTITIONS = 1;

    public static final String[] APP = {"test_app", "test_app.test_app"};

    public static final String TOKEN = "test_token";

    protected static final Logger logger = LoggerFactory.getLogger(TempMetadataInitializer.class);

    private BrokerContext brokerContext;
    private NameService nameService;
    private ServerConfig serverConfig;
    private ClusterManager clusterManager;

    private Broker broker;

    public TempMetadataInitializer(BrokerContext brokerContext) throws Exception {
        this.brokerContext = brokerContext;
        this.clusterManager = brokerContext.getClusterManager();
        this.nameService = clusterManager.getNameService();
        this.serverConfig = brokerContext.getBrokerConfig().getFrontendConfig();
    }

    public static void prepare() {
        try {
            FileUtils.deleteDirectory(new File("/export/Data/journalq"));
            FileUtils.deleteDirectory(new File("/export/Data/ignite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        logger.info("pre init tables");
        //initTable();
        logger.info("init tables");

        logger.info("pre register broker");
        broker = nameService.register(null, serverConfig.getHost(), serverConfig.getPort());
        logger.info("register broker");

        initTopic();
        initToken();
    }

    protected void initTopic() {
        addTopic(TOPIC);
        for (int i = 0; i < TOPIC_COUNT; i++) {
            addTopic(TOPIC + "_" + i);
        }

//        addTopic("test_broadcast_topic", Topic.Type.BROADCAST);
//        addTopic("test_nearby_topic");
//
//        addTopic("test_consume_1");
//        addTopic("test_consume_broadcast_1", Topic.Type.BROADCAST);
//
//        addTopic("test_produce_1");
//        addTopic("test_produce_transaction_1");
//        addTopic("test_produce_broadcast_1", Topic.Type.BROADCAST);
//
//        addTopic("test_produce_nearby_1");
//
//        addTopic("test_produceconsume_1");
//        addTopic("test_produceconsume_broadcast_1", Topic.Type.BROADCAST);
    }

    protected void initToken() {
        for (String app : APP) {
            AppToken appToken = new AppToken();
            appToken.setId(SystemClock.now());
            appToken.setApp(app);
            appToken.setToken(TOKEN);
            appToken.setEffectiveTime(new Date(SystemClock.now() - 1000));
            appToken.setExpirationTime(new Date(SystemClock.now() + 1000 * 60 * 60 * 24 * 7));

            try {
                Field metaManagerField = nameService.getClass().getDeclaredField("metaManager");
                metaManagerField.setAccessible(true);
                MetaManager metaManager = (MetaManager) metaManagerField.get(nameService);

                Field appTokenServiceField = metaManager.getClass().getDeclaredField("appTokenService");
                appTokenServiceField.setAccessible(true);
                AppTokenService appTokenService = (AppTokenService) appTokenServiceField.get(metaManager);

                appTokenService.addOrUpdate(appToken);
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    protected void addTopic(String code) {
        addTopic(code, Topic.Type.TOPIC);
    }

    protected void addTopic(String code, Topic.Type type) {
        addTopic(code, type, PARTITIONS);
    }

    protected void addTopic(String code, Topic.Type type, short partitions) {
        Topic topic = new Topic();
        topic.setName(TopicName.parse(code, NAMESPACE));
        topic.setPartitions(partitions);
        topic.setType(type);
        addTopic(topic);
    }

    protected void addTopic(Topic topic) {
        TopicName topicName = topic.getName();
        List<Producer> producers = Lists.newLinkedList();
        List<Consumer> consumers = Lists.newLinkedList();

        for (String app : APP) {
            Producer producer = new Producer();
            producer.setProducerPolicy(Producer.ProducerPolicy.Builder.build().nearby((topicName.getFullName().contains("nearby"))).archive(true).single(true).
                    blackList("127.0.0.1,127.0.0.2").weight("0:1,1:10").timeout(1000 * 11).create());
            producer.setTopic(topicName);
            producer.setApp(app);
            producer.setClientType(ClientType.JMQ);

            Consumer consumer = new Consumer();
            consumer.setTopic(topicName);
            consumer.setApp(app);
            consumer.setTopicType(topic.getType().equals(Topic.Type.TOPIC) ? TopicType.TOPIC : TopicType.BROADCAST);
            consumer.setClientType(ClientType.JMQ);
            consumer.setRetryPolicy(new RetryPolicy(1000, 1000, 2, false, 2.0, 0));
            consumer.setConsumerPolicy(new Consumer.ConsumerPolicy.Builder().nearby((topicName.getFullName().contains("nearby"))).paused(false).archive(true).
                    retry(false).seq(false).ackTimeout(1000 * 11).batchSize((short) 100).concurrent(1).delay(0).errTimes(10).maxPartitionNum(PARTITION_GROUPS * PARTITIONS).blackList("127.0.0.1,127.0.0.3").retryReadProbability(30).create());

            producers.add(producer);
            consumers.add(consumer);
        }

        List<PartitionGroup> partitionGroups = Lists.newLinkedList();

        for (int j = 0; j < PARTITION_GROUPS; j++) {
            Set<Short> partitions = Sets.newHashSetWithExpectedSize(PARTITIONS);
            for (int k = 0; k < topic.getPartitions(); k++) {
                partitions.add((short) ((j * PARTITIONS) + k));
            }

            PartitionGroup partitionGroup = new PartitionGroup();
            partitionGroup.setTopic(topicName);
            partitionGroup.setGroup(j);
            partitionGroup.setPartitions(partitions);
            partitionGroup.setReplicas(Sets.newHashSet(broker.getId()));
            partitionGroup.setIsrs(Sets.newHashSet(broker.getId()));
            partitionGroup.setLeader(broker.getId());
            partitionGroups.add(partitionGroup);
        }

        logger.info("pre add topic, topic: {}", topic.getName().getFullName());

        nameService.addTopic(topic, partitionGroups);

        for (int i = 0; i < producers.size(); i++) {
            Consumer consumer = consumers.get(i);
            Producer producer = producers.get(i);

            logger.info("pre add producer, topic: {}, app: {}", producer.getTopic(), producer.getApp());
            logger.info("pre add consumer, topic: {}, app: {}", consumer.getTopic(), producer.getApp());

            try {
                Field metaManagerField = nameService.getClass().getDeclaredField("metaManager");
                metaManagerField.setAccessible(true);
                MetaManager metaManager = (MetaManager) metaManagerField.get(nameService);

                Field consumerServiceField = metaManager.getClass().getDeclaredField("consumerService");
                consumerServiceField.setAccessible(true);
                ConsumerService consumerService = (ConsumerService) consumerServiceField.get(metaManager);

                Field producerServiceField = metaManager.getClass().getDeclaredField("producerService");
                producerServiceField.setAccessible(true);
                ProducerService producerService = (ProducerService) producerServiceField.get(metaManager);

                consumerService.add(consumer);
                producerService.add(producer);
            } catch (Exception e) {
                logger.error("", e);
            }

            logger.info("add topic, topic: {}", topic.getName().getFullName());
            logger.info("add producer, topic: {}, app: {}", producer.getTopic(), producer.getApp());
            logger.info("add consumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());
        }
    }
}
