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
package org.joyqueue.nsr.support;

import org.joyqueue.domain.AppToken;
import org.joyqueue.nsr.service.AppTokenService;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;

import java.util.List;

/**
 * DefaultAppTokenService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultAppTokenService implements AppTokenService {

    private AppTokenInternalService appTokenInternalService;

    public DefaultAppTokenService(AppTokenInternalService appTokenInternalService) {
        this.appTokenInternalService = appTokenInternalService;
    }

    @Override
    public AppToken getById(long id) {
        return appTokenInternalService.getById(id);
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        return appTokenInternalService.getByAppAndToken(app, token);
    }

    @Override
    public List<AppToken> getByApp(String app) {
        return appTokenInternalService.getByApp(app);
    }

    @Override
    public List<AppToken> getAll() {
        return appTokenInternalService.getAll();
    }

    @Override
    public AppToken add(AppToken appToken) {
        return appTokenInternalService.add(appToken);
    }

    @Override
    public AppToken update(AppToken appToken) {
        return appTokenInternalService.update(appToken);
    }

    @Override
    public void delete(long id) {
        appTokenInternalService.delete(id);
    }
}