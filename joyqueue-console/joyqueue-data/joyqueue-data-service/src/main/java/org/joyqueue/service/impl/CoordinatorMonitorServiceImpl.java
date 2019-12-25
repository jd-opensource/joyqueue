/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.service.impl;


import com.alibaba.fastjson.JSON;
import org.joyqueue.convert.CodeConverter;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.CoordinatorDetail;
import org.joyqueue.domain.CoordinatorGroup;
import org.joyqueue.domain.CoordinatorGroupExpiredMember;
import org.joyqueue.domain.CoordinatorGroupMember;
import org.joyqueue.domain.CoordinatorGroupMemberExtension;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.monitor.RestResponse;
import org.joyqueue.service.CoordinatorMonitorService;
import org.joyqueue.model.domain.CoordinatorBroker;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.service.LeaderService;
import org.joyqueue.other.HttpRestService;
import org.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//todo 待移走
@Service("coordinatorMonitorService")
public class CoordinatorMonitorServiceImpl implements CoordinatorMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(CoordinatorMonitorServiceImpl.class);
    @Autowired
    HttpRestService httpRestService;
    @Autowired
    LeaderService leaderService;

    @Override
    public CoordinatorGroup findCoordinatorGroup(Subscribe subscribe) {
        CoordinatorDetail coordinatorInfo = findCoordinatorDetail(subscribe);
        Broker coordinator = coordinatorInfo.getCurrent();
        String pathKey = "partitionGroupCoordinatorDetailMonitor";
        String[] args = new String[5];
        args[0] = coordinator.getIp();
        args[1] = String.valueOf(coordinator.getMonitorPort());
        args[2] = ClientType.valueOf(subscribe.getClientType()).getName();
        args[3] = CodeConverter.convertApp(subscribe.getApp(), subscribe.getSubscribeGroup());
        args[4] = CodeConverter.convertTopic(subscribe.getNamespace(), subscribe.getTopic()).getFullName();
        RestResponse<CoordinatorGroup> restCoordinatorGroup = httpRestService.get(pathKey, CoordinatorGroup.class, false, args);
        return restCoordinatorGroup.getData();
    }

    @Override
    public List<CoordinatorBroker> findCoordinatorInfo(Subscribe subscribe) {
        CoordinatorDetail coordinatorDetail = findCoordinatorDetail(subscribe);
        List<CoordinatorBroker> coordinatorBrokers = new ArrayList<>();
        CoordinatorBroker coordinatorBroker = new CoordinatorBroker();
        coordinatorBroker.setBroker(coordinatorDetail.getCurrent());
        coordinatorBroker.setCoordinator(true);
        coordinatorBrokers.add(coordinatorBroker);
        if (!NullUtil.isEmpty(coordinatorDetail.getReplicas())) {
            // CoordinatorBroker coordinatorBroker;
            for (Broker b : coordinatorDetail.getReplicas()) {
                coordinatorBroker = new CoordinatorBroker();
                coordinatorBroker.setBroker(b);
                // coordinatorBroker.setCoordinator(b.equals(coordinatorDetail.getCurrent()));
                coordinatorBrokers.add(coordinatorBroker);
            }
        }
        return coordinatorBrokers;
    }

    /**
     *
     * @return coordinator info
     *
     **/
    public CoordinatorDetail findCoordinatorDetail(Subscribe subscribe) {
        String[] args = new String[3];
        List<Map.Entry<PartitionGroup, org.joyqueue.model.domain.Broker>> partitionGroupBrokers =
                leaderService.findPartitionGroupLeaderBrokerDetail(subscribe.getTopic().getCode(), subscribe.getNamespace().getCode());// default
        if (NullUtil.isEmpty(partitionGroupBrokers)) {
            logger.info("partition group broker not found for {}", JSON.toJSONString(subscribe));
            return new CoordinatorDetail();
        }
        org.joyqueue.model.domain.Broker broker = partitionGroupBrokers.get(0).getValue();
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        args[2] = CodeConverter.convertApp(subscribe.getApp(), subscribe.getSubscribeGroup());
        String pathKey = "partitionGroupCoordinatorInfoMonitor";
        RestResponse<CoordinatorDetail> restCoordinatorDetail = httpRestService.get(pathKey, CoordinatorDetail.class, false, args);
        return restCoordinatorDetail.getData();
    }

    @Override
    public CoordinatorGroupMemberExtension findCoordinatorGroupMember(Subscribe subscribe) {
        List<CoordinatorGroupMember> members = new ArrayList();
        CoordinatorGroupMemberExtension extension = new CoordinatorGroupMemberExtension();
        CoordinatorGroup group = findCoordinatorGroup(subscribe);
        if (!NullUtil.isEmpty(group)) {
            extension.setExtension(group.getExtension());
            if(!NullUtil.isEmpty(group.getMembers())) {
                members.addAll(group.getMembers().values());
            }
        }
        extension.setMembers(members);
        return extension;
    }

    @Override
    public List<CoordinatorGroupExpiredMember> findExpiredCoordinatorGroupMember(Subscribe subscribe) {
        List<CoordinatorGroupExpiredMember> result = new ArrayList();
        CoordinatorGroup group = findCoordinatorGroup(subscribe);
        if (!NullUtil.isEmpty(group) && !NullUtil.isEmpty(group.getExpiredMembers())) {
            result.addAll(group.getExpiredMembers().values());
        }
        return result;
    }
}
