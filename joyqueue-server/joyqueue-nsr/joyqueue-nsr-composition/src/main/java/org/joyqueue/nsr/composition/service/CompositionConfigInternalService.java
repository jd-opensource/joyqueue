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

import com.google.common.collect.Lists;
import org.joyqueue.domain.Config;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionConfigInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConfigInternalService implements ConfigInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionConfigInternalService.class);

    private CompositionConfig config;
    private ConfigInternalService igniteConfigService;
    private ConfigInternalService journalkeeperConfigService;

    public CompositionConfigInternalService(CompositionConfig config, ConfigInternalService igniteConfigService,
                                            ConfigInternalService journalkeeperConfigService) {
        this.config = config;
        this.igniteConfigService = igniteConfigService;
        this.journalkeeperConfigService = journalkeeperConfigService;
    }

    @Override
    public Config getById(String id) {
        return igniteConfigService.getById(id);
    }

    @Override
    public Config getByGroupAndKey(String group, String key) {
        return igniteConfigService.getByGroupAndKey(group, key);
    }

    @Override
    public List<Config> getAll() {
        List<Config> configs = igniteConfigService.getAll();
        List<Config> result = Lists.newArrayList(configs);
        result.add(0, new Config(null, "current.nameserver.composition.read.source", this.config.getReadSource()));
        result.add(1, new Config(null, "current.nameserver.composition.write.source", this.config.getWriteSource()));
        return result;
    }

    @Override
    public Config add(Config config) {
        Config result = igniteConfigService.add(config);
        if (this.config.isWriteJournalkeeper()) {
            try {
                journalkeeperConfigService.add(config);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", config, e);
            }
        }
        return result;
    }

    @Override
    public Config update(Config config) {
        Config result = igniteConfigService.update(config);
        if (this.config.isWriteJournalkeeper()) {
            try {
                journalkeeperConfigService.update(config);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", config, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        igniteConfigService.delete(id);
        if (this.config.isWriteJournalkeeper()) {
            try {
                journalkeeperConfigService.delete(id);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
