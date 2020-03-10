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
package org.joyqueue.broker.manage.service;

import org.joyqueue.manage.IndexItem;
import org.joyqueue.manage.PartitionGroupMetric;
import org.joyqueue.manage.PartitionMetric;
import org.joyqueue.manage.TopicMetric;
import org.joyqueue.toolkit.io.Directory;

import java.io.File;
import java.util.List;

/**
 * StoreManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/18
 */
public interface StoreManageService {

    /**
     * 获取所有主题的度量信息
     *
     * @return 主题度量信息
     */
    TopicMetric[] topicMetrics();

    /**
     * 获取主题的度量信息
     *
     * @param topic 主题
     * @return 主题度量信息
     */
    TopicMetric topicMetric(String topic);

    /**
     * 获取主题下分区组的度量信息
     *
     * @param topic 主题
     * @param partitionGroup 分区组
     * @return 分区组度量信息
     */
    PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup);

    /**
     * 获取主题下分区的度量信息
     *
     * @param topic 主题
     * @param partition 分区
     * @return 分区度量信息
     */
    PartitionMetric partitionMetric(String topic, short partition);

    /**
     * 获取目录下所有文件
     *
     * @param path 目录
     * @return 文件列表
     */
    File[] listFiles(String path);

    /**
     * 获取绝对路径目录下所有文件
     *
     * @param path 目录
     * @return 文件列表
     */
    File[] listAbsolutePathFiles(String path);

    /**
     * 移除主题
     *
     * @param topic 主题
     */
    void removeTopic(String topic);

    /**
     * Store tree view
     * @param recursive  recurse child directory if true
     **/
    Directory storeTreeView(boolean recursive);

    /**
     * 删除已被软删除的文件
     * @param fileName  file name
     * @param retain  保留目录 if true
     **/
    boolean deleteGarbageFile(String fileName,boolean retain);
    /**
     * 获取所有主题名
     *
     * @return 主题名列表
     */
    List<String> topics();

    /**
     * 获取主题下所有分区的度量信息
     *
     * @param topic 主题
     * @return 分区度量信息列表
     */
    List<PartitionGroupMetric> partitionGroups(String topic);

    /**
     * 获取主题下分区组指针处的消息
     *
     * @param topic 主题
     * @param partitionGroup 分区组
     * @param position 指针
     * @param count 数量
     * @return 格式化后的消息列表
     */
    List<String> readPartitionGroupMessage(String topic, int partitionGroup, long position, int count);

    /**
     * 获取主题下分区索引处的消息
     *
     * @param topic 主题
     * @param partition 分区
     * @param index 索引
     * @param count 数量
     * @return 格式化后的消息列表
     */
    List<String> readPartitionMessage(String topic, short partition, long index, int count);

    /**
     * 获取文件中指针处的消息
     *
     * @param file 文件
     * @param position 指针
     * @param count 数量
     * @param includeFileHeader 是否包含文件头
     * @return 格式化后的消息列表
     */
    List<String> readMessage(String file, long position, int count, boolean includeFileHeader);

    /**
     * 获取主题下分区的索引信息
     *
     * @param topic 主题
     * @param partition 分区
     * @param index 索引
     * @param count 数量
     * @return 索引信息列表
     */
    IndexItem [] readPartitionIndices(String topic, short partition, long index, int count);

    /**
     * 获取文件中指针处的索引信息
     *
     * @param file 文件
     * @param position 指针
     * @param count 数量
     * @param includeFileHeader 是否包含文件头
     * @return 索引信息列表
     */
    IndexItem [] readIndices(String file, long position, int count, boolean includeFileHeader);

    /**
     * 读文件
     *
     * @param file 文件
     * @param position 指针
     * @param length 长度
     * @return 格式化后的内容
     */
    String readFile(String file, long position, int length);

    /**
     * 读主题下分区组的文件
     *
     * @param topic 主题
     * @param partitionGroup 分区组
     * @param position 指针
     * @param length 长度
     * @return 格式化后的内容
     */
    String readPartitionGroupStore(String topic, int partitionGroup, long position, int length);
}