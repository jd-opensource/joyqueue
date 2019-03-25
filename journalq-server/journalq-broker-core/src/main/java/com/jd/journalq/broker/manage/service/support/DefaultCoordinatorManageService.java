package com.jd.journalq.broker.manage.service.support;

import com.jd.journalq.broker.coordinator.CoordinatorGroupManager;
import com.jd.journalq.broker.coordinator.CoordinatorService;
import com.jd.journalq.broker.manage.service.CoordinatorManageService;

/**
 * DefaultCoordinatorManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class DefaultCoordinatorManageService implements CoordinatorManageService {

    private CoordinatorService coordinatorService;

    public DefaultCoordinatorManageService(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    @Override
    public boolean initCoordinator() {
        return coordinatorService.getCoordinator().initCoordinator();
    }

    @Override
    public boolean removeCoordinatorGroup(String namespace, String groupId) {
        CoordinatorGroupManager coordinatorGroupManager = coordinatorService.getOrCreateCoordinatorGroupManager(namespace);
        return coordinatorGroupManager.removeGroup(groupId);
    }
}