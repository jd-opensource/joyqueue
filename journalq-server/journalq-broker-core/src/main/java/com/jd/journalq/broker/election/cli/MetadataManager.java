package com.jd.journalq.broker.election.cli;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.jd.journalq.broker.election.ElectionMetadata;
import com.jd.journalq.broker.election.ElectionMetadataManager;
import com.jd.journalq.broker.election.TopicPartitionGroup;

import java.io.File;


public class MetadataManager {
    private ElectionMetadataManager electionMetadataManager;

    private MetadataManager(File file) throws Exception {
        electionMetadataManager = new ElectionMetadataManager(file);
        electionMetadataManager.start();
    }

    public static void main(String [] args) {
        if(args.length < 1) {
            showUsage();
            return;
        }

        if (args.length < 3) {
            System.out.println("Invalid arguments number");
            return;
        }

        File metadataFile = new File(args[0]);
        if(metadataFile.isDirectory()) {
            System.out.println("Invalid filename!");
            return;
        }

        String operation = args[1];
        String partitionGroup  = args[2];

        System.out.println(String.format("Operation %s %s", operation, partitionGroup));

        MetadataManager metadataManager;
        try {
            metadataManager = new MetadataManager(metadataFile);
        } catch (Exception e) {
            System.out.println("Create metadata manager fail" + e);
            return;
        }

        switch (operation) {
            case "-a": {
                String metadata = args[3];
                metadataManager.addMetadataItem(partitionGroup, metadata);
                break;
            }
            case "-d": {
                metadataManager.delMetadataItem(partitionGroup);
                break;
            }
            case "-u": {
                String metadata = args[3];
                metadataManager.uptMetadataItem(partitionGroup, metadata);
                break;
            }
            default: {
                System.out.println("Invalid operation " + operation);
            }
        }
    }


    private static void showUsage() {
        System.out.println("Usage: MetadataManager filename {-a partition-group metadata | -d partition-group | -u partition-group metadata}");
    }

    private void addMetadataItem(String partitionGroup, String metadata) {
        TopicPartitionGroup topicPartitionGroup = JSON.parseObject(partitionGroup, new TypeReference<TopicPartitionGroup>() {});
        ElectionMetadata electionMetadata = JSON.parseObject(metadata, new TypeReference<ElectionMetadata>() {});
        electionMetadataManager.updateElectionMetadata(topicPartitionGroup, electionMetadata);
    }

    private void delMetadataItem(String partitionGroup) {
        TopicPartitionGroup topicPartitionGroup = JSON.parseObject(partitionGroup, new TypeReference<TopicPartitionGroup>() {});
        electionMetadataManager.removeElectionMetadata(topicPartitionGroup);
    }

    private void uptMetadataItem(String partitionGroup, String metadata) {
        TopicPartitionGroup topicPartitionGroup = JSON.parseObject(partitionGroup, new TypeReference<TopicPartitionGroup>() {});
        ElectionMetadata electionMetadata = JSON.parseObject(metadata, new TypeReference<ElectionMetadata>() {});
        electionMetadataManager.updateElectionMetadata(topicPartitionGroup, electionMetadata);
    }
}