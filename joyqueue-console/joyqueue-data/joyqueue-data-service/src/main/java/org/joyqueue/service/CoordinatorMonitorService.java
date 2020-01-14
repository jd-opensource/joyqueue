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
package org.joyqueue.service;

import org.joyqueue.domain.CoordinatorGroup;
import org.joyqueue.domain.CoordinatorGroupExpiredMember;
import org.joyqueue.domain.CoordinatorGroupMemberExtension;
import org.joyqueue.model.domain.CoordinatorBroker;
import org.joyqueue.model.domain.Subscribe;

import java.util.List;
//todo 待移走
/**
 *
 * @author  wangjin18
 * @date    2019-01-02
 *
 **/
public interface CoordinatorMonitorService {


    /**
     *
     * @return app  消费组的协调信息，包括当前的协调信息和已过期的协调信息
     *
     * */
    CoordinatorGroup findCoordinatorGroup(Subscribe subscribe);

    /**
     *
     * @return  app 的协调者信息
     *
     **/
    List<CoordinatorBroker> findCoordinatorInfo(Subscribe subscribe);


    /**
     *
     * @return app  消费组成员列表
     *
     * */
    CoordinatorGroupMemberExtension findCoordinatorGroupMember(Subscribe subscribe);


    /**
     *
     * @return app  过期的消费组成员列表
     *
     * */
    List<CoordinatorGroupExpiredMember> findExpiredCoordinatorGroupMember(Subscribe subscribe);



}
