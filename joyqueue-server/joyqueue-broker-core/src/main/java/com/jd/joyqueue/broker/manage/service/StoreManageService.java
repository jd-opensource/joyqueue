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
package com.jd.joyqueue.broker.manage.service;

import com.jd.joyqueue.manage.IndexItem;
import com.jd.joyqueue.manage.PartitionGroupMetric;
import com.jd.joyqueue.manage.PartitionMetric;
import com.jd.joyqueue.manage.TopicMetric;

import java.io.File;
import java.util.List;

/**
 * StoreManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/18
 */
public interface StoreManageService {

    /**
     * Store度量信息
     */
    TopicMetric[] topicMetrics();

    TopicMetric topicMetric(String topic);

    PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup);

    PartitionMetric partitionMetric(String topic, short partition);

    /**
     * 列出store中给定path的所有文件
     *
     * @param path 相对store根目录的相对路径
     */
    File[] listFiles(String path);

    File[] listAbsolutePathFiles(String path);

    void removeTopic(String topic);

    List<String> topics();

    /**
     * 读取partition
     * @param topic
     * @return
     */
    List<PartitionGroupMetric> partitionGroups(String topic);

    /**
     * 读取消息
     */
    List<String> readPartitionGroupMessage(String topic, int partitionGroup, long position, int count);

    List<String> readPartitionMessage(String topic, short partition, long index, int count);

    List<String> readMessage(String file, long position, int count, boolean includeFileHeader);

    /**
     * 读取索引
     */
    IndexItem [] readPartitionIndices(String topic, short partition, long index, int count);

    IndexItem [] readIndices(String file, long position, int count, boolean includeFileHeader);

    /**
     * 裸接口
     */
    String readFile(String file, long position, int length);

    String readPartitionGroupStore(String topic, int partitionGroup, long position, int length);
}