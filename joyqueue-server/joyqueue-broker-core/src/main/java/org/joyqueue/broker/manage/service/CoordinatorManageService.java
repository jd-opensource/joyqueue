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
package org.joyqueue.broker.manage.service;

/**
 * CoordinatorManageService
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public interface CoordinatorManageService {

    /**
     * 初始化协调者
     *
     * @return 是否成功
     */
    boolean initCoordinator();

    /**
     * 移除协调组信息
     *
     * @param namespace 作用域
     * @param groupId 组
     * @return 是否成功
     */
    boolean removeCoordinatorGroup(String namespace, String groupId);
}