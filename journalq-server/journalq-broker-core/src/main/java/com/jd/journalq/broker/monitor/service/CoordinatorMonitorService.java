package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.coordinator.group.domain.GroupMemberMetadata;
import com.jd.journalq.broker.coordinator.group.domain.GroupMetadata;

import java.util.Map;

/**
 * CoordinatorMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public interface CoordinatorMonitorService {

    /**
     * 查找协调者
     *
     * @param groupId
     * @return
     */
    CoordinatorDetail getCoordinator(String groupId);

    /**
     * 获得协调者组
     *
     * @param namespace
     * @param groupId
     * @param topic
     * @param isFormat
     * @return
     */
    GroupMetadata getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat);

    /**
     * 获得协调者组成员
     *
     * @param namespace
     * @param groupId
     * @param topic
     * @param isFormat
     * @return
     */
    Map<String, GroupMemberMetadata> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat);
}