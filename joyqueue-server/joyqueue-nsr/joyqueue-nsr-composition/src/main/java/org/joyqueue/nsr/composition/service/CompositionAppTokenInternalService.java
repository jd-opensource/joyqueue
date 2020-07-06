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
    private AppTokenInternalService igniteAppTokenService;
    private AppTokenInternalService journalkeeperAppTokenService;

    public CompositionAppTokenInternalService(CompositionConfig config, AppTokenInternalService igniteAppTokenService,
                                              AppTokenInternalService journalkeeperAppTokenService) {
        this.config = config;
        this.igniteAppTokenService = igniteAppTokenService;
        this.journalkeeperAppTokenService = journalkeeperAppTokenService;
    }

    @Override
    public List<AppToken> getByApp(String app) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getByApp(app);
        } else {
            try {
                return journalkeeperAppTokenService.getByApp(app);
            } catch (Exception e) {
                logger.error("getByApp exception, app: {}", app, e);
                return igniteAppTokenService.getByApp(app);
            }
        }
    }

    @Override
    public AppToken getByAppAndToken(String app, String token) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getByAppAndToken(app, token);
        } else {
            try {
                return journalkeeperAppTokenService.getByAppAndToken(app, token);
            } catch (Exception e) {
                logger.error("getByAppAndToken exception, app: {}, token: {}", app, token, e);
                return igniteAppTokenService.getByAppAndToken(app, token);
            }
        }
    }

    @Override
    public AppToken getById(long id) {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getById(id);
        } else {
            try {
                return journalkeeperAppTokenService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return igniteAppTokenService.getById(id);
            }
        }
    }

    @Override
    public List<AppToken> getAll() {
        if (config.isReadIgnite()) {
            return igniteAppTokenService.getAll();
        } else {
            try {
                return journalkeeperAppTokenService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return igniteAppTokenService.getAll();
            }
        }
    }

    @Override
    public AppToken add(AppToken appToken) {
        AppToken result = null;
        if (config.isWriteIgnite()) {
            result = igniteAppTokenService.add(appToken);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                return journalkeeperAppTokenService.add(appToken);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", appToken, e);
            }
        }
        return result;
    }

    @Override
    public AppToken update(AppToken appToken) {
        AppToken result = null;
        if (config.isWriteIgnite()) {
            result = igniteAppTokenService.update(appToken);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                return journalkeeperAppTokenService.update(appToken);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", appToken, e);
            }
        }
        return result;
    }

    @Override
    public void delete(long id) {
        if (config.isWriteIgnite()) {
            igniteAppTokenService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperAppTokenService.delete(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
