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
package org.joyqueue.nsr;

import org.joyqueue.model.domain.ApplicationToken;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface AppTokenNameServerService {

    ApplicationToken findById(Long id) throws Exception;

    int delete(ApplicationToken model) throws Exception;

    int add(ApplicationToken model) throws Exception;

    int update(ApplicationToken model) throws Exception;

    List<ApplicationToken> findByApp(String app) throws Exception;

    ApplicationToken findByAppAndToken(String app, String token) throws Exception;
}
