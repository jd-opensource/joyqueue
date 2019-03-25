//package com.jd.journalq.server.broker.kafka.coordinator.listener;
//
//import com.google.common.collect.Lists;
//import Broker;
//import TopicName;
//import com.jd.journalq.server.broker.cluster.ClusterManager;
//import com.jd.journalq.server.broker.election.ElectionEvent;
//import com.jd.journalq.server.broker.kafka.coordinator.GroupCoordinatorResolver;
//import com.jd.journalq.server.broker.kafka.coordinator.GroupMetadataManager;
//import com.jd.journalq.server.broker.kafka.coordinator.model.GroupMetadata;
//import EventListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//
///**
// * 用缓存代替
// * GroupCoordinatorChangeListener
// * author: gaohaoxiang
// * email: gaohaoxiang@jd.com
// * date: 2018/11/8
// */
//@Deprecated
//public class GroupCoordinatorChangeListener implements EventListener<ElectionEvent> {
//
//    protected static final Logger logger = LoggerFactory.getLogger(GroupCoordinatorChangeListener.class);
//
//    private ClusterManager clusterManager;
//    private GroupMetadataManager groupMetadataManager;
//    private GroupCoordinatorResolver groupCoordinatorResolver;
//
//    public GroupCoordinatorChangeListener(ClusterManager clusterManager, GroupMetadataManager groupMetadataManager, GroupCoordinatorResolver groupCoordinatorResolver) {
//        this.clusterManager = clusterManager;
//        this.groupMetadataManager = groupMetadataManager;
//        this.groupCoordinatorResolver = groupCoordinatorResolver;
//    }
//
//    @Override
//    public void onEvent(ElectionEvent event) {
//        if (!groupCoordinatorResolver.isCoordinatorTopic(new TopicName(event.getTopicPartitionGroup().getTopic()))) {
//            return;
//        }
//
//        Broker currentBroker = clusterManager.getBroker();
//        List<GroupMetadata> invalidGroups = Lists.newLinkedList();
//        List<GroupMetadata> groups = groupMetadataManager.getGroups();
//        for (GroupMetadata group : groups) {
//            Broker coordinatorBroker = groupCoordinatorResolver.resolve(group.getGroupId());
//            if (coordinatorBroker == null || coordinatorBroker.getId() != currentBroker.getId()) {
//                invalidGroups.add(group);
//            }
//        }
//
//        for (GroupMetadata group : invalidGroups) {
//            logger.info("group coordinator changed, groupId: {}", group.getGroupId());
//            groupMetadataManager.removeGroup(group);
//        }
//    }
//}