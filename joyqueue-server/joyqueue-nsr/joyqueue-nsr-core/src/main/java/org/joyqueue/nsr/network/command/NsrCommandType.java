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
package org.joyqueue.nsr.network.command;

import org.joyqueue.network.command.CommandType;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrCommandType {
    // 布尔应答 -128
    public static final int BOOLEAN_ACK = CommandType.BOOLEAN_ACK;
    public static final int CONNECT = 0;
    // nameservice.leaderReport
    public static final int LEADER_REPORT = 6;
    public static final int LEADER_REPORT_ACK = -6;
    //nameservice.subscribe 100 and -100 兼容mqtt通过joyqueue client 请求
    public static final int SUBSCRIBE = 100;
    public static final int SUBSCRIBE_ACK = -100;
    //nameservice.unsubscribe 101
    public static final int UN_SUBSCRIBE = 101;

    public static final int MQTT_GET_TOPICS = 102;
    public static final int MQTT_GET_TOPICS_ACK = -102;

    public static final int AUTHORIZATION = 103;
    // nameservice.hasSubscrite
    public static final int HAS_SUBSCRIBE = 7;
    public static final int HAS_SUBSCRIBE_ACK = -7;
    // nameservice.getBroker
    public static final int GET_BROKER = 8;
    // nameservice.getBroker
    public static final int GET_BROKER_ACK = -8;
    // nameservice.getAllBrokers
    public static final int GET_ALL_BROKERS = 9;
    // nameservice.getAllBrokers
    public static final int GET_ALL_BROKERS_ACK = -9;
    // nameservice.getTopicConfig
    public static final int GET_TOPICCONFIG = 10;
    // nameservice.getTopicConfig
    public static final int GET_TOPICCONFIG_ACK = -10;
    // nameservice.getTopics
    public static final int GET_ALL_TOPICS = 11;
    // nameservice.getTopics
    public static final int GET_ALL_TOPICS_ACK = -11;
    // nameservice.getTopics 41 and -41
    public static final int GET_TOPICS = CommandType.GET_TOPICS;
    // nameservice.getTopics
    public static final int GET_TOPICS_ACK = CommandType.GET_TOPICS_ACK;
    // nameservice.getTopics
    public static final int GET_TOPICCONFIGS_BY_BROKER = 13;
    // nameservice.getTopics
    public static final int GET_TOPICCONFIGS_BY_BROKER_ACK = -13;
    // nameservice.getTopics
    public static final int REGISTER = 14;
    // nameservice.getTopics
    public static final int REGISTER_ACK = -14;
    // nameservice.getProducerByTopicAndApp
    public static final int GET_PRODUCER_BY_TOPIC_AND_APP = 15;
    public static final int GET_PRODUCER_BY_TOPIC_AND_APP_ACK = -15;
    // nameservice.getConsumerByTopicAndApp
    public static final int GET_CONSUMER_BY_TOPIC_AND_APP = 16;
    public static final int GET_CONSUMER_BY_TOPIC_AND_APP_ACK = -16;
    // nameservice.getTopicConfigByApp
    public static final int GET_TOPICCONFIGS_BY_APP = 17;
    public static final int GET_TOPICCONFIGS_BY_APP_ACK = -17;
    // nameservice.getDatacenter
    public static final int GET_DATACENTER = 18;
    public static final int GET_DATACENTER_ACK = -18;
    // nameservice.getConfig
    public static final int GET_CONFIG = 19;
    public static final int GET_CONFIG_ACK = -19;
    // nameservice.getAllConfig
    public static final int GET_ALL_CONFIG = 20;
    public static final int GET_ALL_CONFIG_ACK = -20;
    // nameservice.getBrokerByRetryType
    public static final int GET_BROKER_BY_RETRYTYPE = 21;
    public static final int GET_BROKER_BY_RETRYTYPE_ACK = -21;
    // nameservice.getConsumerByTopic
    public static final int GET_CONSUMER_BY_TOPIC = 22;
    public static final int GET_CONSUMER_BY_TOPIC_ACK = -22;
    // nameservice.getProducerByTopic
    public static final int GET_PRODUCER_BY_TOPIC = 23;
    public static final int GET_PRODUCER_BY_TOPIC_ACK = -23;
    //nameservice.getReplicaByBroker
    public static final int GET_REPLICA_BY_BROKER = 24;
    public static final int GET_REPLICA_BY_BROKER_ACK = -24;
    //nameservice.getAppToekn
    public static final int GET_APP_TOKEN = 25;
    public static final int GET_APP_TOKEN_ACK = -25;
    //nameservice.getAppToekn
    public static final int PUSH_NAMESERVER_EVENT = 26;
    public static final int PUSH_NAMESERVER_EVENT_ACK = -26;

    public static final int ADD_TOPIC = 27;
    //新建partitionGroup
    public static final int NSR_CREATE_PARTITIONGROUP = 127;
    //更新partitionGroup
    public static final int NSR_UPDATE_PARTITIONGROUP = 126;
    //删除partitionGroup
    public static final int NSR_REMOVE_PARTITIONGROUP = 125;
    //删除partitionGroup
    public static final int NSR_LEADERCHANAGE_PARTITIONGROUP = 124;

    // 推送请求
    public static final int NSR_MESSENGER_PUBLISH_REQUEST = 50;
    // 心跳
    public static final int NSR_MESSENGER_HEARTBEAT_REQUEST = 51;
    // 返回所有元数据
    public static final int NSR_GET_ALL_METADATA_REQUEST = 52;
    public static final int NSR_GET_ALL_METADATA_RESPONSE = -52;
}
