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
package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.response.BooleanResponse;

import java.util.List;

/**
 * MetadataMonitorService
 *
 * author: gaohaoxiang
 * date: 2019/2/11
 */
public interface MetadataMonitorService {

    /**
     * 获取主题元数据
     *
     * @param topic 主题
     * @param isCluster true - 从ClusterManager获取，false - 从NameServer获取
     * @return 主题元数据
     */
    TopicConfig getTopicMetadata(String topic, boolean isCluster);

    /**
     * 是否有读权限
     *
     * @param topic 主题
     * @param app 应用
     * @param address 地址
     * @return 结果
     */
    BooleanResponse getReadableResult(String topic, String app, String address);

    /**
     * 是否有写权限
     *
     * @param topic 主题
     * @param app 应用
     * @param address 地址
     * @return 结果
     */
    BooleanResponse getWritableResult(String topic, String app, String address);

    /**
     * 获取消费者元数据
     *
     * @param topic 主题
     * @param app 应用
     * @param isCluster true - 从ClusterManager获取，false - 从NameServer获取
     * @return 元数据
     */
    Consumer getConsumerMetadataByTopicAndApp(String topic, String app, boolean isCluster);

    /**
     * 获取生产者元数据
     *
     * @param topic 主题
     * @param app 应用
     * @param isCluster true - 从ClusterManager获取，false - 从NameServer获取
     * @return 元数据
     */
    Producer getProducerMetadataByTopicAndApp(String topic, String app, boolean isCluster);

    /**
     * 导出数据
     * @return
     */
    Object exportMetadata(String source);

    /**
     * 同步数据
     * @param source
     * @param target
     * @param interval
     * @return
     */
    Object syncMetadata(String source, String target, int interval);

    /**
     * 查询元数据
     * @param operator
     * @param params
     * @return
     */
    Object queryMetadata(String source, String operator, List<Object> params);

    /**
     * 插入元数据
     * @param source
     * @param operator
     * @param params
     * @return
     */
    Object insertMetadata(String source, String operator, List<Object> params);

    /**
     * 更新元数据
     * @param source
     * @param operator
     * @param params
     * @return
     */
    Object updateMetadata(String source, String operator, List<Object> params);

    /**
     * 删除元数据
     * @param source
     * @param operator
     * @param params
     * @return
     */
    Object deleteMetadata(String source, String operator, List<Object> params);
}