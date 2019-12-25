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
package org.joyqueue.nsr.ignite.service;

import com.google.inject.Inject;
import org.joyqueue.domain.Config;
import org.joyqueue.event.ConfigEvent;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.ignite.dao.ConfigDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.model.IgniteConfig;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgniteConfigInternalService implements ConfigInternalService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private ConfigDao configDao;

    @Inject
    public IgniteConfigInternalService(ConfigDao configDao) {
        this.configDao = configDao;
    }

    @Inject
    protected IgniteMessenger messenger;

    public Config getByGroupAndKey(String group, String key) {
        return getById(IgniteConfig.getId(group, key));
    }

    @Override
    public Config add(Config config) {
        try {
            configDao.addOrUpdate(new IgniteConfig(config));
            this.publishEvent(ConfigEvent.add(config.getGroup(), config.getKey(), config.getValue()));
            return config;
        } catch (Exception e) {
            String message = String.format("add config group [%s] key [%s] error", config.getGroup(), config.getKey());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public Config update(Config config) {
        try {
            configDao.addOrUpdate(new IgniteConfig(config));
            this.publishEvent(ConfigEvent.update(config.getGroup(), config.getKey(), config.getValue()));
            return config;
        } catch (Exception e) {
            String message = String.format("update config group [%s] key [%s] error", config.getGroup(), config.getKey());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public void delete(String id) {
        IgniteConfig config = configDao.findById(id);
        try {
            configDao.deleteById(id);
            this.publishEvent(ConfigEvent.remove(config.getGroup(), config.getKey(), config.getValue()));
        } catch (Exception e) {
            String message = String.format("remove config group [%s] key [%s] error", config.getGroup(), config.getKey());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public void publishEvent(MetaEvent event) {
        messenger.publish(event);
    }

    public IgniteConfig toIgniteModel(Config model) {
        return new IgniteConfig(model);
    }

    @Override
    public Config getById(String id) {
        return configDao.findById(id);
    }

    @Override
    public List<Config> getAll() {
        return convert(configDao.list(null));
    }

    private List<Config> convert(List<IgniteConfig> iConfigs) {
        if (iConfigs == null) {
            return Collections.emptyList();
        }

        List<Config> configs = new ArrayList<>();
        configs.addAll(iConfigs);
        return configs;
    }
}



