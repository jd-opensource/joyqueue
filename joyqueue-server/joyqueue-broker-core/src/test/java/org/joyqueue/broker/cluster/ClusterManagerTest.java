/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.broker.cluster;

import org.joyqueue.broker.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author wylixiaobin
 * Date: 2018/9/13
 */
// FIXME: 单元测试不通过
public class ClusterManagerTest {
    private final Logger logger = LoggerFactory.getLogger(ClusterManager.class);
//
//    private ClusterManager clusterManager;
//    @Before
//    public void setUp() throws Exception {
//        /**
//         * 传入brokerId 则测试对应的broker,不传则测试本机器对应的broker
//         */
//        clusterManager = null;//new ClusterManager(null);
//        clusterManager.start();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        clusterManager.stop();
//    }
//
//    @Test
//    public void getDataCenterByIP() {
//        logger.info("ip[{},datacenter[{}]]",IpUtil.getLocalIp(),clusterManager.getDataCenterByIP(IpUtil.getLocalIp()));
//    }
//
//    @Test
//    public void getBrokerId() {
//        logger.info("getBrokerId,{}",clusterManager.getBrokerId());
//    }
//
//    @Test
//    public void getConfig() {
//        logger.info("getConfig,{}",JSON.toJSONString(clusterManager.getConfig()));
//    }
//
//    @Test
//    public void getBroker() {
//        logger.info("getBroker,{}",JSON.toJSONString(clusterManager.getBroker()));
//    }
//
//
//    @Test
//    public void getBrokerByPartition() {
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        short partitionId = 1;
//        clusterManager.getTopics().forEach(topicConfig -> {
//            topicConfig.getPartitionGroups().values().forEach(
//                    group->{
//                        group.getPartitions().forEach(partition->{
//                            logger.info("topic[{}],partition[{}],getBrokerByPartition[{}]",topicConfig.getName(),partition,JSON.toJSONString(clusterManager.getBrokerByPartition(topicConfig.getName(),partition)));
//                        });
//                    }
//            );
//        });
//        logger.info("topic[{}],partition[{}],getBrokerByPartition[{}]",topic,partitionId,JSON.toJSONString(clusterManager.getBrokerByPartition(topic,partitionId)));
//    }
//
//    @Test
//    public void getPartitionGroupByGroup() {
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        short groupId = 1;
//        clusterManager.getTopics().forEach(topicConfig -> {
//            topicConfig.getPartitionGroups().values().forEach(
//                    group->{
//                        logger.info("topic[{}],group[],getPartitionGroupByGroup[{}]",topicConfig.getName(),group.getGroup(),JSON.toJSONString(clusterManager.getPartitionGroupByGroup(topicConfig.getName(),group.getGroup())));
//                    }
//            );
//        });
//        logger.info("topic[{}],partition[{}],getPartitionGroupByGroup[{}]",topic,groupId,JSON.toJSONString(clusterManager.getPartitionGroupByGroup(topic,groupId)));
//    }
//
//    @Test
//    public void getTopicConfig() {
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("getTopics[{}]",JSON.toJSONString(clusterManager.getTopicConfig(topicConfig.getName())));
//        });
//        logger.info("topic[{}],getTopics[{}]",topic,JSON.toJSONString(clusterManager.getTopicConfig(topic)));
//        System.out.println(JSON.toJSONString(clusterManager.getTopicConfig(topic)));
//    }
//
//    @Test
//    public void getTopics() {
//        logger.info("getTopics[{}]",JSON.toJSONString(clusterManager.getTopics()));
//    }
//
//    @Test
//    public void getPartitionGroup() {
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("topic[{}] ,getPartitionGroup[{}]",topicConfig.getName(),JSON.toJSONString(clusterManager.getPartitionGroup(topicConfig.getName())));
//        });
//        logger.info("topic[{}] getPartitionGroup[{}]",topic,JSON.toJSONString(clusterManager.getReplicaGroup(topic)));
//    }
//
//    @Test
//    public void getPartitionGroupId() {
//        String app = "joyqueue";
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        short partitionNum = 1;
//        clusterManager.getTopics().forEach(topicConfig -> {
//                    topicConfig.getPartitionGroups().values().forEach(group -> {
//                        group.getPartitions().forEach(partition -> {
//                            logger.info("topic[{}] partition[{}] ,getPartitionGroupId[{}]", topicConfig.getName(), partition, JSON.toJSONString(clusterManager.getPartitionGroupId(topicConfig.getName(), partition)));
//                        });
//                    });
//                });
//        logger.info("topic[{}] partition[{}] getPartitionGroupId[{}]",topic,app,JSON.toJSONString(clusterManager.getPartitionGroupId(topic,partitionNum)));
//    }
//
//    @Test
//    public void getReplicaGroup() {
//        String app = "joyqueue";
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("topic[{}] app[{}] ,getReplicaGroup[{}]",topicConfig.getName(),app,JSON.toJSONString(clusterManager.getReplicaGroup(topicConfig.getName())));
//        });
//        logger.info("topic[{}] app[{}] getReplicaGroup[{}]",topic,app,JSON.toJSONString(clusterManager.getReplicaGroup(topic)));
//    }
//
//    @Test
//    public void getPartitionList() {
//        String app = "joyqueue";
//        String ip = IpUtil.getLocalIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("topic[{}] app[{}] ,getPartitionList[{}]",topicConfig.getName(),app,JSON.toJSONString(clusterManager.getPartitionList(topicConfig.getName())));
//        });
//        logger.info("topic[{}] app[{}] getPartitionList[{}]",topic,app,JSON.toJSONString(clusterManager.getPartitionList(topic)));
//    }
//
//    @Test
//    public void getMasterPartitionList() {
//        String app = "joyqueue";
//        String ip = IpUtil.getLocalIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("topic[{}] app[{}] ,getMasterPartitionList[{}]",topicConfig.getName(),app,JSON.toJSONString(clusterManager.getMasterPartitionList(topicConfig.getName())));
//        });
//        logger.info("topic[{}] app[{}] getMasterPartitionList[{}]",topic,app,JSON.toJSONString(clusterManager.getMasterPartitionList(topic)));
//    }
//
//    @Test
//    public void getPriorityPartitionList() {
//        String app = "joyqueue";
//        String ip = IpUtil.getLocalIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("topic[{}] app[{}] ,getPriorityPartitionList[{}]",topicConfig.getName(),app,JSON.toJSONString(clusterManager.getPriorityPartitionList(topicConfig.getName())));
//        });
//        logger.info("topic[{}] app[{}] getPriorityPartitionList[{}]",topic,app,JSON.toJSONString(clusterManager.getPriorityPartitionList(topic)));
//    }
//
//    @Test
//    public void getConsumerPolicy() throws JoyQueueException {
//        String app = "joyqueue";
//        String ip = IpUtil.getLocalIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        for (TopicConfig topicConfig : clusterManager.getTopics()) {
//            logger.info("topic[{}] app[{}] ,getConsumerPolicy[{}]", topicConfig.getName(), app, JSON.toJSONString(clusterManager.getConsumerPolicy(topicConfig.getName(), app)));
//        }
//        logger.info("topic[{}] app[{}] getConsumerPolicy[{}]",topic,app,JSON.toJSONString(clusterManager.getConsumerPolicy(topic,app)));
//    }
//
//    @Test
//    public void isNeedNearby() throws JoyQueueException {
//        String app = "joyqueue";
//        String ip = IpUtil.getLocalIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        for (TopicConfig topicConfig : clusterManager.getTopics()) {
//            logger.info("topic[{}] app[{}] ,isNeedNearby[{}]", topicConfig.getName(), app, clusterManager.isNeedNearby(topicConfig.getName(), app));
//        }
//        logger.info("topic[{}] app[{}] isNeedNearby[{}]",topic,app,clusterManager.isNeedNearby(topic,app));
//    }
//
//    @Test
//    public void getAckTimeout() throws JoyQueueException {
//        String app = "joyqueue";
//        String ip = IpUtil.getLocalIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        for (TopicConfig topicConfig : clusterManager.getTopics()) {
//            logger.info("topic[{}] app[{}] ,getAckTimeout[{}]", topicConfig.getName(), app, clusterManager.getAckTimeout(topicConfig.getName(), app));
//        }
//        logger.info("topic[{}] app[{}] getAckTimeout[{}]",topic,app,clusterManager.getAckTimeout(topic,app));
//    }
//
//    @Test
//    public void getProducerPolicy() {
//    }
//
//    @Test
//    public void checkWritable() throws JoyQueueException {
//        String app = "joyqueue";
//        String ip = clusterManager.getBroker().getIp();
//        TopicName topic = TopicName.parse("joyqueue@Test");
//        for (TopicConfig topicConfig : clusterManager.getTopics()) {
//            logger.info("topic[{}] app[{}] ip [{}],checkWritable[{}]", topicConfig.getName(), app, ip, clusterManager.checkWritable(topicConfig.getName(), app, IpUtil.getLocalIp()));
//        }
//        logger.info("topic[{}] app[{}] ip [{}],checkWritable[{}]",topic,app,ip,clusterManager.checkWritable(topic,app,IpUtil.getLocalIp()));
//    }
//
//    @Test
//    public void checkReadable() {
//        String app = "joyqueue";
//        String ip = clusterManager.getBroker().getIp();
//        String topic = "joyqueue@Test";
//        clusterManager.getTopics().forEach(topicConfig -> {
//            logger.info("topic[{}] app[{}] ip [{}],checkReadable[{}]",topicConfig.getName(),app,ip,clusterManager.checkReadable(topicConfig.getName(),app,IpUtil.getLocalIp()));
//        });
//        logger.info("topic[{}] app[{}] ip [{}],checkReadable[{}]",topic,app,ip,clusterManager.checkReadable(TopicName.parse(topic),app,IpUtil.getLocalIp()));
//    }
//
//    @Test
//    public void leaderReport() {
//    }
}