package com.jd.journalq.broker.election;

import com.alibaba.fastjson.TypeReference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.common.domain.PartitionGroup;
import com.jd.journalq.common.domain.TopicConfig;
import com.jd.journalq.toolkit.io.Files;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 选举中需要保存的metadata，每个PartitionGroup需要保存节点信息，raft还需要保存currentTerm和voteFor
 * 在每次重新选举时都需要保存metadata信息，选举任务启动时恢复metadata
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class ElectionMetadataManager extends Service {
    private static Logger logger = LoggerFactory.getLogger(ElectionMetadataManager.class);

    private Map<TopicPartitionGroup, ElectionMetadata> metadataMap = new ConcurrentHashMap<>();
    private File file;

    public ElectionMetadataManager(File file) throws IOException {
        this.file = file;
        if (!Files.createFile(file)) {
            throw new IOException(String.format("Create file error,%s", file.getPath()));
        }
        if (!file.canWrite()) {
            throw new IOException(String.format("File %s can not be written.", file.getPath()));
        }
        if (!file.canRead()) {
            throw new IOException(String.format("File %s can not be read.", file.getPath()));
        }
    }

    @Override
    public void doStart() throws Exception{
        super.doStart();

        loadElectionMetadata();
    }

    @Override
    public void doStop() {
        super.doStop();
    }

    /**
     * 通过PartitionGroup获取election metadata
     * @param topicPartitionGroup topic 和 partition group id
     * @return 元数据
     */
    ElectionMetadata getElectionMetadata(TopicPartitionGroup topicPartitionGroup) {
        return metadataMap.get(topicPartitionGroup);
    }


    /**
     * 加载metadata
     * @throws Exception exception
     */
    private synchronized void loadElectionMetadata() throws Exception{
        String metadataStr = (String) readConfigFile(file, String.class, "");
        if (metadataStr != null && !metadataStr.isEmpty()) {
            metadataMap = JSON.parseObject(metadataStr,
                    new TypeReference<ConcurrentHashMap<TopicPartitionGroup, ElectionMetadata>>() {
                    });
        }
    }

    /**
     * 更新metadata信息，每次更新都直接写到文件
     * @param topicPartitionGroup topic 和 partition group id
     * @param metadata 元数据
     */
    public synchronized void updateElectionMetadata(TopicPartitionGroup topicPartitionGroup, ElectionMetadata metadata) {
        try {
            metadataMap.put(topicPartitionGroup, metadata);
            String metadataStr = JSON.toJSONString(metadataMap, SerializerFeature.DisableCircularReferenceDetect);
            writeConfigFile(file, metadataStr);
        } catch (Exception e) {
            logger.error("Update election metadata of {} fail", topicPartitionGroup, e);
        }
    }

    /**
     * 删除partition group对应的election metadata， 每次删除都更新文件
     * @param topicPartitionGroup topic 和 partition group id
     */
    public synchronized void removeElectionMetadata(TopicPartitionGroup topicPartitionGroup) {
        try {
            metadataMap.remove(topicPartitionGroup);
            String metadataStr = JSON.toJSONString(metadataMap);
            writeConfigFile(file, metadataStr);
        } catch (Exception e) {
            logger.error("Remove election metadata of {} fail", topicPartitionGroup, e);
        }
    }

    /**
     * 根据metadata恢复leader election
     * @throws Exception exception
     */
    synchronized void restoreLeaderElections(ElectionManager electionManager) throws Exception {
        for (TopicPartitionGroup topicPartitionGroup : metadataMap.keySet()) {
            try {
                electionManager.restoreLeaderElection(topicPartitionGroup, metadataMap.get(topicPartitionGroup));
            } catch (Exception e) {
                logger.warn("Restore leader election fail", e);
            }
        }
    }

    private ElectionMetadata generateMetadataFromPartitionGroup(PartitionGroup partitionGroup, int localBrokerId) {
        ElectionMetadata metadata = new ElectionMetadata();

        metadata.setElectType(partitionGroup.getElectType());
        metadata.setLeaderId(partitionGroup.getLeader());
        metadata.setLearners(partitionGroup.getLearners());
        metadata.setLocalNodeId(localBrokerId);
        metadata.setCurrentTerm(partitionGroup.getTerm());
        metadata.setAllNodes(partitionGroup.getBrokers().values().stream()
                .map(broker -> new DefaultElectionNode(broker.getAddress(), broker.getId()))
                .collect(Collectors.toList()));

        return metadata;
    }

    /**
     * 从name service同步选举元数据
     * @param clusterManager 集群管理
     */
    synchronized void syncElectionMetadataFromNameService(ClusterManager clusterManager) {
        metadataMap.clear();

        List<TopicConfig> topicConfigs = clusterManager.getTopics();
        for (TopicConfig topicConfig : topicConfigs) {
            topicConfig.getPartitionGroups().values().forEach((pg) -> {
                ElectionMetadata metadata = generateMetadataFromPartitionGroup(pg, clusterManager.getBrokerId());
                updateElectionMetadata(new TopicPartitionGroup(topicConfig.getName().getFullName(), pg.getGroup()), metadata);
            });
        }
    }

    private static Object readConfigFile(File file, Class objClass, Object defValue) throws IOException {
        if (file == null || !file.exists()) {
            if (defValue == null)
                throw new IOException("file is null or not exists");
            else
                return defValue;
        }

        byte[] buf;
        try(FileInputStream reader = new FileInputStream(file)) {
            if (reader.available() > 10 * 1024 * 1024) {
                throw new IOException("file " + file.getAbsolutePath() + " is too large to process");
            } else if (reader.available() == 0) {
                return defValue;
            }
            buf = new byte[reader.available()];
            reader.read(buf);
        }

        if (objClass.equals(String.class)) {
            return new String(buf);
        } else {
            Object retObj = JSON.parseObject(buf, objClass);
            return (retObj == null ? defValue : retObj);
        }
    }

    private static void writeConfigFile(File file, Object content) throws IOException {
        BufferedWriter writer = null;
        try {
            if (file != null) {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                writer = new BufferedWriter(new FileWriter(file));
                if (content instanceof String) {
                    writer.write((String) content);
                } else {
                    writer.write(JSON.toJSONString(content));
                }
                writer.flush();
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

}

