package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import io.chubao.joyqueue.broker.coordinator.group.domain.GroupMemberMetadata;
import io.chubao.joyqueue.broker.coordinator.group.domain.GroupMetadata;

import java.util.Map;

/**
 * CoordinatorMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public interface CoordinatorMonitorService {

    /**
     * 获取组的协调者详情
     *
     * @param groupId 组
     * @return 协调者详情
     */
    CoordinatorDetail getCoordinator(String groupId);

    /**
     * 获得协调者组元数据
     *
     * @param namespace 作用域
     * @param groupId 组
     * @param topic 主题
     * @param isFormat 是否格式化元数据
     * @return 组元数据
     */
    GroupMetadata getCoordinatorGroup(String namespace, String groupId, String topic, boolean isFormat);

    /**
     * 获得协调者组成员
     *
     * @param namespace 作用域
     * @param groupId 组
     * @param topic 主题
     * @param isFormat 是否格式化元数据
     * @return 所有组成员
     */
    Map<String, GroupMemberMetadata> getCoordinatorGroupMembers(String namespace, String groupId, String topic, boolean isFormat);
}