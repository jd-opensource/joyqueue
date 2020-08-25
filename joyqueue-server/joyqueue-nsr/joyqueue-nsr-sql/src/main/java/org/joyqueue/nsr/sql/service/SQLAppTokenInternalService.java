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
package org.joyqueue.nsr.sql.service;

import org.joyqueue.domain.AppToken;
import org.joyqueue.nsr.sql.converter.AppTokenConverter;
import org.joyqueue.nsr.sql.domain.AppTokenDTO;
import org.joyqueue.nsr.sql.repository.AppTokenRepository;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;

import java.util.List;

/**
 * SQLAppTokenInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLAppTokenInternalService implements AppTokenInternalService {

    private AppTokenRepository appTokenRepository;

    public SQLAppTokenInternalService(AppTokenRepository appTokenRepository) {
        this.appTokenRepository = appTokenRepository;
    }

    @Override
    public AppToken getById(long id) {
        return AppTokenConverter.convert(appTokenRepository.getById(id));
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        return AppTokenConverter.convert(appTokenRepository.getByAppAndToken(app, token));
    }

    @Override
    public List<AppToken> getByApp(String app) {
        return AppTokenConverter.convert(appTokenRepository.getByApp(app));
    }

    @Override
    public List<AppToken> getAll() {
        return AppTokenConverter.convert(appTokenRepository.getAll());
    }

    @Override
    public AppToken add(AppToken appToken) {
        AppTokenDTO appTokenDTO = appTokenRepository.add(AppTokenConverter.convert(appToken));
        return AppTokenConverter.convert(appTokenDTO);
    }

    @Override
    public AppToken update(AppToken appToken) {
        AppTokenDTO appTokenDTO = appTokenRepository.update(AppTokenConverter.convert(appToken));
        return AppTokenConverter.convert(appTokenDTO);
    }

    @Override
    public void delete(long id) {
        appTokenRepository.deleteById(id);
    }
}
