package com.jd.journalq.broker.manage.service;

/**
 * CoordinatorManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public interface CoordinatorManageService {

    /**
     * 初始化协调者主题
     *
     * @return
     */
    public boolean initCoordinator();

    /**
     * 移除协调者组
     *
     * @param namespace
     * @param groupId
     * @return
     */
    public boolean removeCoordinatorGroup(String namespace, String groupId);
}