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
    private ConfigInternalService sourceConfigService;
    private ConfigInternalService targetConfigService;

    public CompositionConfigInternalService(CompositionConfig config, ConfigInternalService sourceConfigService,
                                            ConfigInternalService targetConfigService) {
        this.config = config;
        this.sourceConfigService = sourceConfigService;
        this.targetConfigService = targetConfigService;
    }

    @Override
    public Config getById(String id) {
        return sourceConfigService.getById(id);
    }

    @Override
    public Config getByGroupAndKey(String group, String key) {
        return sourceConfigService.getByGroupAndKey(group, key);
    }

    @Override
    public List<Config> getAll() {
        List<Config> configs = sourceConfigService.getAll();
        List<Config> result = Lists.newArrayList(configs);
        result.add(0, new Config(null, "current.nameserver.composition.read.source", this.config.getReadSource()));
        result.add(1, new Config(null, "current.nameserver.composition.write.source", this.config.getWriteSource()));
        result.add(2, new Config(null, "current.nameserver.composition.source", this.config.getSource()));
        result.add(3, new Config(null, "current.nameserver.composition.target", this.config.getTarget()));
        return result;
    }

    @Override
    public Config add(Config config) {
        Config result = sourceConfigService.add(config);
        if (this.config.isWriteTarget()) {
            try {
                targetConfigService.add(config);
            } catch (Exception e) {
                logger.error("update exception, params: {}", config, e);
            }
        }
        return result;
    }

    @Override
    public Config update(Config config) {
        Config result = sourceConfigService.update(config);
        if (this.config.isWriteTarget()) {
            try {
                targetConfigService.update(config);
            } catch (Exception e) {
                logger.error("update exception, params: {}", config, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        sourceConfigService.delete(id);
        if (this.config.isWriteTarget()) {
            try {
                targetConfigService.delete(id);
            } catch (Exception e) {
                logger.error("update exception, params: {}", id, e);
            }
        }
    }
}
