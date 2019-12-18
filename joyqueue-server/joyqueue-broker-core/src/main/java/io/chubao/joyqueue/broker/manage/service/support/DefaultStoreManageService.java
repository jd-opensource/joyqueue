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
package io.chubao.joyqueue.broker.manage.service.support;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.manage.converter.StoreManageConverter;
import io.chubao.joyqueue.broker.manage.service.StoreManageService;
import io.chubao.joyqueue.manage.*;
import io.chubao.joyqueue.store.PartitionGroupStore;
import io.chubao.joyqueue.store.StoreManagementService;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.store.message.MessageParser;
import io.chubao.joyqueue.toolkit.io.Directory;
import io.chubao.joyqueue.toolkit.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StoreManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/18
 */
public class DefaultStoreManageService implements StoreManageService {
    private Logger logger= LoggerFactory.getLogger(DefaultStoreManageService.class);
    private static final String TOPICS_DIR = "topics";
    private static final String DEL_PREFIX = ".d.";
    private StoreManagementService storeManagementService;
    private ClusterManager clusterManager;
    private StoreService storeService;
    public DefaultStoreManageService(StoreManagementService storeManagementService, ClusterManager clusterManager, StoreService storeService) {
        this.storeManagementService = storeManagementService;
        this.clusterManager=clusterManager;
        this.storeService=storeService;
    }

    @Override
    public TopicMetric[] topicMetrics() {
        return StoreManageConverter.convert(storeManagementService.storeMetrics());
    }

    @Override
    public TopicMetric topicMetric(String topic) {
        return StoreManageConverter.convert(storeManagementService.topicMetric(topic));
    }

    @Override
    public PartitionGroupMetric partitionGroupMetric(String topic, int partitionGroup) {
        return StoreManageConverter.convert(storeManagementService.partitionGroupMetric(topic, partitionGroup));
    }

    @Override
    public PartitionMetric partitionMetric(String topic, short partition) {
        return StoreManageConverter.convert(storeManagementService.partitionMetric(topic, partition));
    }

    @Override
    public File[] listFiles(String path) {
        return storeManagementService.listFiles(path);
    }

    @Override
    public File[] listAbsolutePathFiles(String path) {
        return storeManagementService.listFiles(new File(path));
    }

    @Override
    public void removeTopic(String topic) {
    }

    @Override
    public Directory storeTreeView(boolean recursive) {
        String topics=TOPICS_DIR;
        Directory directory=new Directory();
        directory.setName(topics);
        File[] topicList=storeManagementService.listFiles(topics);
        directory.setDirectory(true);
        if(topicList.length>0){
            directory.setChildren(new ArrayList());
        }
        //Arrays.sort(topicList,Comparator.comparing(File::getName));
        Files.sortByName(topicList);
        // recurse to child
        for (File f : topicList) {
            Directory child = new Directory();
            Files.tree(f.getPath(),recursive, child);
            directory.getChildren().add(child);
        }
        return directory;
    }

    @Override
    public boolean deleteGarbageFile(String fileName,boolean retain) {
        File file=new File(fileName);
        if(file.exists()&&file.getName().contains(DEL_PREFIX)){
            if(retain&&file.isDirectory()){
                 for(File f:file.listFiles()){
                     Files.deleteDirectory(f);
                 }
            }else {
                Files.deleteDirectory(file);
            }
            logger.info("delete file {}",file.getName());
            return true;
        }
        return false;
    }

    @Override
    public List<String> topics() {
        return Arrays.stream(storeManagementService.listFiles(TOPICS_DIR)).map(file -> file.getName()).collect(Collectors.toList());
    }

    @Override
    public List<SortedTopic> sortedTopics() {
        List<String> topics=topics();
        List<SortedTopic> sortedTopics=new ArrayList();
        for(String t:topics){
            SortedTopic sortedTopic=new SortedTopic();
            sortedTopic.setTopic(t);
            String partitionGroupPath=TOPICS_DIR+"/"+t;
            File[] partitionGroupFiles = storeManagementService.listFiles(partitionGroupPath);
            long topicTotalStorageSize=0;
            int leaders=0;
            for (File file :partitionGroupFiles) {
               int partitionGroup= Long.valueOf(file.getName()).intValue();
                PartitionGroupStore pgStore = storeService.getStore(t,partitionGroup);
                if(pgStore!=null) {
                    topicTotalStorageSize+=pgStore.getTotalPhysicalStorageSize();
                }
                leaders+=clusterManager.isLeader(t,partitionGroup)?1:0;
            }
            sortedTopic.setPartitionGroups(partitionGroupFiles.length);
            sortedTopic.setPartitionGroupLeaders(leaders);
            sortedTopic.setValue(topicTotalStorageSize);
            sortedTopics.add(sortedTopic);
        }
        sortedTopics.sort(Comparator.comparing(SortedTopic::getValue).reversed());
        int i=0;
        for(SortedTopic t:sortedTopics){
            t.setOrder(++i);
        }
        return sortedTopics;
    }

    @Override
    public List<PartitionGroupMetric> partitionGroups(String topic) {
        String partitionGroupPath=TOPICS_DIR+"/"+topic;
        File[] partitionGroupFiles = storeManagementService.listFiles(partitionGroupPath);
        List<PartitionGroupMetric> partitionGroupMetrics = new ArrayList<>();
        for (File file :partitionGroupFiles) {
            PartitionGroupMetric partitionGroupMetric = new PartitionGroupMetric();
            String partitionFile=partitionGroupPath+"/"+file.getName()+"/index";
            File[] partitionFiles = storeManagementService.listFiles(partitionFile);
            String partitions= Arrays.stream(partitionFiles).map(file1 -> file1.getName()).sorted(Comparator.comparingInt(Integer::valueOf)).collect(Collectors.joining(","));
            if (StringUtils.isNumeric(file.getName())) {
                partitionGroupMetric.setPartitionGroup(Long.valueOf(file.getName()).intValue());
            }
            partitionGroupMetric.setPartitions(partitions);
            // leader and storage info
            partitionGroupMetric.setLeader(clusterManager.isLeader(topic,partitionGroupMetric.getPartitionGroup()));
            PartitionGroupStore pgStore = storeService.getStore(topic,partitionGroupMetric.getPartitionGroup());
            if(pgStore!=null) {
                partitionGroupMetric.setStorageSize(pgStore.getTotalPhysicalStorageSize());
            }
            partitionGroupMetrics.add(partitionGroupMetric);
        }
        return partitionGroupMetrics;
    }

    @Override
    public List<String> readPartitionGroupMessage(String topic, int partitionGroup, long position, int count) {
        byte[][] messages = storeManagementService.readMessages(topic, partitionGroup, position, count);
        List<String> result = Lists.newLinkedList();
        for (byte[] message : messages) {
            result.add(MessageParser.getString(ByteBuffer.wrap(message)));
        }
        return result;
    }

    @Override
    public List<String> readPartitionMessage(String topic, short partition, long index, int count) {
        byte[][] messages = storeManagementService.readMessages(topic, partition, index, count);
        List<String> result = Lists.newLinkedList();
        for (byte[] message : messages) {
            result.add(MessageParser.getString(ByteBuffer.wrap(message)));
        }
        return result;
    }

    @Override
    public List<String> readMessage(String file, long position, int count, boolean includeFileHeader) {
        byte[][] messages = storeManagementService.readMessages(new File(file), position, count, includeFileHeader);
        List<String> result = Lists.newLinkedList();
        for (byte[] message : messages) {
            result.add(MessageParser.getString(ByteBuffer.wrap(message)));
        }
        return result;
    }

    @Override
    public IndexItem [] readPartitionIndices(String topic, short partition, long index, int count) {
        return StoreManageConverter.convert(storeManagementService.readIndices(topic, partition, index, count));
    }

    @Override
    public IndexItem [] readIndices(String file, long position, int count, boolean includeFileHeader) {
        return StoreManageConverter.convert(storeManagementService.readIndices(new File(file), position, count, includeFileHeader));
    }

    @Override
    public String readFile(String file, long position, int length) {
        byte[] message = storeManagementService.readFile(new File(file), position, length);
        return MessageParser.getString(ByteBuffer.wrap(message));
    }

    @Override
    public String readPartitionGroupStore(String topic, int partitionGroup, long position, int length) {
        byte[] message = storeManagementService.readPartitionGroupStore(topic, partitionGroup, position, length);
        return MessageParser.getString(ByteBuffer.wrap(message));
    }
}