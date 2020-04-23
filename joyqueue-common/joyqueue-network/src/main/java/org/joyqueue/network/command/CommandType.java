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
package org.joyqueue.network.command;

/**
 * @author wylixiaobin
 * Date: 2018/10/8
 * heartbeat 0
 *
 */
public class CommandType {

    // 布尔应答
    public static final int BOOLEAN_ACK = -128;
    // 存放重试消息
    public static final int PUT_RETRY = 6;
    // 获取重试消息
    public static final int GET_RETRY = 7;
    // 获取重试消息应答
    public static final int GET_RETRY_ACK = -7;
    // 更新重试消息
    public static final int UPDATE_RETRY = 8;
    // 获取重试条数
    public static final int GET_RETRY_COUNT = 9;
    // 获取重试条数应答
    public static final int GET_RETRY_COUNT_ACK = -9;
    //订阅
    public static final int SUBSCRIBE = 38;
    //订阅响应
    public static final int SUBSCRIBE_ACK = 39;

    //取消订阅
    public static final int UNSUBSCRIBE = 40;
    //MQTT 查询所有的topic
    public static final int GET_TOPICS = 41;
    public static final int GET_TOPICS_ACK = -41;
    // raft选举投票请求命令
    public static final int RAFT_VOTE_REQUEST = 43;
    // raft选举投票请求命令响应
    public static final int RAFT_VOTE_RESPONSE = -43;
    // raft复制添加记录命令
    public static final int RAFT_APPEND_ENTRIES_REQUEST = 45;
    // raft复制添加记录命令响应
    public static final int RAFT_APPEND_ENTRIES_RESPONSE = -45;
    // 立即选举请求命令
    public static final int RAFT_TIMEOUT_NOW_REQUEST = 46;
    // 立即选举响应命令
    public static final int RAFT_TIMEOUT_NOW_RESPONSE = -46;
    // 查询index请求命令
    public static final int CONSUME_INDEX_QUERY_REQUEST = 47;
    // 查询index响应命令
    public static final int CONSUME_INDEX_QUERY_RESPONSE = -47;
    // 保存index请求命令
    public static final int CONSUME_INDEX_STORE_REQUEST = 48;
    // 保存index响应命令
    public static final int CONSUME_INDEX_STORE_RESPONSE = -48;
    // 复制消费位置请求命令
    public static final int REPLICATE_CONSUME_POS_REQUEST = 49;
    // 复制消费位置响应命令
    public static final int REPLICATE_CONSUME_POS_RESPONSE = -49;

    public static final int AUTHORIZATION = 63;
    //create partitionGroup
    public static final int NSR_CREATE_PARTITIONGROUP = 127;
    //update partitionGroup
    public static final int NSR_UPDATE_PARTITIONGROUP = 126;
    //remove partitionGroup
    public static final int NSR_REMOVE_PARTITIONGROUP = 125;
    //change leader
    public static final int NSR_LEADERCHANAGE_PARTITIONGROUP = 124;

    // 事务
    // 事务提交
    public static final int TRANSACTION_COMMIT_REQUEST = 70;
    // 事务回滚
    public static final int TRANSACTION_ROLLBACK_REQUEST = 71;

    // 主题组
    public static final int GET_PARTITION_GROUP_CLUSTER_REQUEST = 72;
    public static final int GET_PARTITION_GROUP_CLUSTER_RESPONSE = -72;
}