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
package org.joyqueue.sync;

import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.User;

/**
 * 同步服务
 */
public interface SyncService {

    /**
     * 同步应用
     *
     * @param application
     * @return
     * @throws Exception
     */
    ApplicationInfo syncApp(Application application) throws Exception;

    /**
     * 同步应用
     *
     * @param user
     * @return
     * @throws Exception
     */
    UserInfo syncUser(User user) throws Exception;

    /**
     * 更新应用数据
     *
     * @param info
     * @return
     */
    Application addOrUpdateApp(ApplicationInfo info);

    /**
     * 更新用户数据
     *
     * @param info
     * @return
     */
    User addOrUpdateUser(UserInfo info);
}
