package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroup;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroupMember;

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
    public CoordinatorDetail getCoordinator(String groupId);

    /**
     * 获得协调者组
     *
     * @param namespace
     * @param groupId
     * @param topic
     * @param isFormat
     * @return
     */
    public CoordinatorGroup getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat);

    /**
     * 获得协调者组成员
     *
     * @param namespace
     * @param groupId
     * @param topic
     * @param isFormat
     * @return
     */
    public Map<String, CoordinatorGroupMember> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat);
}