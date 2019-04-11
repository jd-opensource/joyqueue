/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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