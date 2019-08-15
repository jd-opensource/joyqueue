package io.chubao.joyqueue.broker.manage.service.support;

import io.chubao.joyqueue.broker.coordinator.CoordinatorService;
import io.chubao.joyqueue.broker.coordinator.group.GroupMetadataManager;
import io.chubao.joyqueue.broker.manage.service.CoordinatorManageService;

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
        GroupMetadataManager groupMetadataManager = coordinatorService.getOrCreateGroupMetadataManager(namespace);
        return groupMetadataManager.removeGroup(groupId);
    }
}