package com.jd.journalq.broker.manage.service.support;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.manage.converter.StoreManageConverter;
import com.jd.journalq.broker.manage.service.StoreManageService;
import com.jd.journalq.common.manage.IndexItem;
import com.jd.journalq.common.manage.PartitionGroupMetric;
import com.jd.journalq.common.manage.PartitionMetric;
import com.jd.journalq.common.manage.TopicMetric;
import com.jd.journalq.store.StoreManagementService;
import com.jd.journalq.store.message.MessageParser;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StoreManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/18
 */
public class DefaultStoreManageService implements StoreManageService {
    private final static String TOPICS_DIR = "topics";

    private StoreManagementService storeManagementService;

    public DefaultStoreManageService(StoreManagementService storeManagementService) {
        this.storeManagementService = storeManagementService;
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
    public List<String> topics() {
        return Arrays.stream(storeManagementService.listFiles(TOPICS_DIR)).map(file -> file.getName()).collect(Collectors.toList());
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
            String partitions= Arrays.stream(partitionFiles).map(file1 -> file1.getName()).sorted().collect(Collectors.joining(","));
            partitionGroupMetric.setPartitionGroup(Long.valueOf(file.getName()).intValue());
            partitionGroupMetric.setPartitions(partitions);
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