package com.jd.journalq.broker;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.domain.*;
import com.jd.journalq.network.transport.config.ServerConfig;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.retry.RetryPolicy;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    public static final int TOPIC_COUNT = 5;

    public static final short PARTITION_GROUPS = 10;

    public static final short PARTITIONS = 1;

    public static final String APP = "test_app";

    public static final String TOKEN = "test_token";

    protected static final Logger logger = LoggerFactory.getLogger(TempMetadataInitializer.class);

    private BrokerContext brokerContext;
    private NameService nameService;
    private ServerConfig serverConfig;
    private ClusterManager clusterManager;

    public TempMetadataInitializer(BrokerContext brokerContext) throws Exception {
        this.brokerContext = brokerContext;
        this.clusterManager = brokerContext.getClusterManager();
        this.nameService = clusterManager.getNameService();
        this.serverConfig = brokerContext.getBrokerConfig().getFrontendConfig();
    }

    public static void prepare() {
        try {
//            FileUtils.copyFile(new File("/export/Data/jmq/store/stat"), new File("/export/Data/jmq_stat"));
            FileUtils.deleteDirectory(new File("/export/Data/jmq"));
//            FileUtils.forceMkdir(new File("/export/Data/jmq/store"));
//            FileUtils.copyFile(new File("/export/Data/jmq_stat"), new File("/export/Data/jmq/store/stat"));
        } catch (IOException e) {
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
        Broker broker = nameService.register(null, serverConfig.getHost(), serverConfig.getPort());
        logger.info("register broker");

        initTopic(broker);
    }

    protected void initTopic(Broker broker) {
        for (int i = 0; i < TOPIC_COUNT; i++) {
            TopicName topicName = TopicName.parse(TOPIC + "_" + i);
            Producer producer = new Producer();
            producer.setProducerPolicy(Producer.ProducerPolicy.Builder.build().nearby(true).archive(true).single(true).
                    blackList("127.0.0.1,127.0.0.2").weight("0:1,1:10").timeout(1000 * 11).create());
            producer.setTopic(topicName);
            producer.setApp(APP);
            producer.setClientType(ClientType.JMQ);

            Consumer consumer = new Consumer();
            consumer.setTopic(topicName);
            consumer.setApp(APP);
            consumer.setClientType(ClientType.JMQ);
            consumer.setRetryPolicy(new RetryPolicy(1000, 1000, 2, false, 2.0, 0));
            consumer.setConsumerPolicy(new Consumer.ConsumerPolicy.Builder().nearby(true).paused(false).archive(true).
                    retry(false).seq(false).ackTimeout(1000 * 11).batchSize((short) 100).concurrent(1).delay(0).errTimes(10).maxPartitionNum(5).blackList("127.0.0.1,127.0.0.3").retryReadProbability(30).create());

            Topic topic = new Topic();
            topic.setName(topicName);
            topic.setPartitions(PARTITIONS);
            topic.setType(Topic.Type.TOPIC);

            List<PartitionGroup> partitionGroups = Lists.newLinkedList();

            for (int j = 0; j < PARTITION_GROUPS; j++) {
                Set<Short> partitions = Sets.newHashSetWithExpectedSize(PARTITIONS);
                for (int k = 0; k < PARTITIONS; k++) {
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
            logger.info("pre add producer, topic: {}, app: {}", producer.getTopic(), producer.getApp());
            logger.info("pre add consumer, topic: {}, app: {}", consumer.getTopic(), producer.getApp());

            nameService.addTopic(topic, partitionGroups);
            //TODO 这里准备做啥
            //BeanContext.getObject(ProducerService.class).add(producer);
            //BeanContext.getObject(ConsumerService.class).add(consumer);

//            new Thread(() -> {
//                try {
//                    Thread.currentThread().sleep(1000 * 30);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                logger.info("remove consumer, topic: {}", topic);
//                Subscription subscription = new Subscription();
//                subscription.setTopic(topicName);
//                subscription.setApp(APP);
//                subscription.setType(Subscription.Type.CONSUMPTION);
//                nameService.unSubscribe(subscription);
//                logger.info("remove consumer success, topic: {}", topic);
//            }).start();

            logger.info("add topic, topic: {}", topic.getName().getFullName());
            logger.info("add producer, topic: {}, app: {}", producer.getTopic(), producer.getApp());
            logger.info("add consumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

//            if (clusterManager.getTopicConfig(topic.getName()) == null) {
//                throw new RuntimeException("topic init failed, data is not exist");
//            }
//
//            if (!clusterManager.checkWritable(topic.getName(), producer.getApp(), null).isSuccess()) {
//                throw new RuntimeException("producer init failed, is unwritable");
//            }
//
//            if (!clusterManager.checkReadable(consumer.getTopic(), consumer.getApp(), null).isSuccess()) {
//                throw new RuntimeException("consumer init failed, is unreadable");
//            }
        }

        AppToken appToken = new AppToken();
        appToken.setId(SystemClock.now());
        appToken.setApp(APP);
        appToken.setToken(TOKEN);
        appToken.setEffectiveTime(new Date(SystemClock.now() - 1000));
        appToken.setExpirationTime(new Date(SystemClock.now() + 1000 * 60 * 60 * 24 * 7));
        //TODO 这里准备做啥
       // BeanContext.getObject(AppTokenService.class).addOrUpdate(appToken);
    }

    //TODO delete
  /*  protected void initTable() {
        metaManager.getIgnite().execute(IgniteConsumer.cacheCfg, new SqlFieldsQuery("ALTER TABLE igniteConsumer ADD COLUMN (app varchar(100),namespace varchar(100),topic varchar(100));"));
        metaManager.getIgnite().execute(IgniteConsumerConfig.cacheCfg, new SqlFieldsQuery("ALTER TABLE igniteConsumerConfig ADD COLUMN (app varchar(100),namespace varchar(100),topic varchar(100));"));
        //metaManager.getIgnite().execute(IgniteProducer.cacheCfg,new SqlFieldsQuery("ALTER TABLE IgniteProducer ADD COLUMN (app varchar(100),namespace varchar(100),topic varchar(100));"));
        metaManager.getIgnite().execute(IgnitePartitionGroup.cacheCfg, new SqlFieldsQuery("ALTER TABLE ignitepartitiongroup ADD COLUMN (namespace varchar(100),topic varchar(100));"));
    }*/
}
