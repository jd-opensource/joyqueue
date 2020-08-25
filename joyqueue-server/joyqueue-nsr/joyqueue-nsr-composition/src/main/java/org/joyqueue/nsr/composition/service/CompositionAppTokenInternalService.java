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
package org.joyqueue.nsr.composition.service;

import org.joyqueue.domain.AppToken;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionAppTokenInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionAppTokenInternalService implements AppTokenInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionAppTokenInternalService.class);

    private CompositionConfig config;
    private AppTokenInternalService sourceAppTokenService;
    private AppTokenInternalService targetAppTokenService;

    public CompositionAppTokenInternalService(CompositionConfig config, AppTokenInternalService sourceAppTokenService,
                                              AppTokenInternalService targetAppTokenService) {
        this.config = config;
        this.sourceAppTokenService = sourceAppTokenService;
        this.targetAppTokenService = targetAppTokenService;
    }

    @Override
    public List<AppToken> getByApp(String app) {
        if (config.isReadSource()) {
            return sourceAppTokenService.getByApp(app);
        } else {
            try {
                return targetAppTokenService.getByApp(app);
            } catch (Exception e) {
                logger.error("getByApp exception, app: {}", app, e);
                return sourceAppTokenService.getByApp(app);
            }
        }
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        if (config.isReadSource()) {
            return sourceAppTokenService.getByAppAndToken(app, token);
        } else {
            try {
                return targetAppTokenService.getByAppAndToken(app, token);
            } catch (Exception e) {
                logger.error("getByAppAndToken exception, app: {}, token: {}", app, token, e);
                return sourceAppTokenService.getByAppAndToken(app, token);
            }
        }
    }

    @Override
    public AppToken getById(long id) {
        if (config.isReadSource()) {
            return sourceAppTokenService.getById(id);
        } else {
            try {
                return targetAppTokenService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourceAppTokenService.getById(id);
            }
        }
    }

    @Override
    public List<AppToken> getAll() {
        if (config.isReadSource()) {
            return sourceAppTokenService.getAll();
        } else {
            try {
                return targetAppTokenService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceAppTokenService.getAll();
            }
        }
    }

    @Override
    public AppToken add(AppToken appToken) {
        AppToken result = null;
        if (config.isWriteSource()) {
            result = sourceAppTokenService.add(appToken);
        }
        if (config.isWriteTarget()) {
            try {
                return targetAppTokenService.add(appToken);
            } catch (Exception e) {
                logger.error("add exception, params: {}", appToken, e);
            }
        }
        return result;
    }

    @Override
    public AppToken update(AppToken appToken) {
        AppToken result = null;
        if (config.isWriteSource()) {
            result = sourceAppTokenService.update(appToken);
        }
        if (config.isWriteTarget()) {
            try {
                return targetAppTokenService.update(appToken);
            } catch (Exception e) {
                logger.error("update exception, params: {}", appToken, e);
            }
        }
        return result;
    }

    @Override
    public void delete(long id) {
        if (config.isWriteSource()) {
            sourceAppTokenService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetAppTokenService.delete(id);
            } catch (Exception e) {
                logger.error("deleteById exception, params: {}", id, e);
            }
        }
    }
}
