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

import org.joyqueue.domain.Config;
import org.joyqueue.nsr.sql.converter.ConfigConverter;
import org.joyqueue.nsr.sql.repository.ConfigRepository;
import org.joyqueue.nsr.service.internal.ConfigInternalService;

import java.util.List;

/**
 * SQLConfigInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLConfigInternalService implements ConfigInternalService {

    private ConfigRepository configRepository;

    public SQLConfigInternalService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public Config getById(String id) {
        return ConfigConverter.convert(configRepository.getById(id));
    }

    @Override
    public Config getByGroupAndKey(String group, String key) {
        return ConfigConverter.convert(configRepository.getByKeyAndGroup(key, group));
    }

    @Override
    public Config add(Config config) {
        return ConfigConverter.convert(configRepository.add(ConfigConverter.convert(config)));
    }

    @Override
    public Config update(Config config) {
        return ConfigConverter.convert(configRepository.update(ConfigConverter.convert(config)));
    }

    @Override
    public void delete(String id) {
        configRepository.deleteById(id);
    }

    @Override
    public List<Config> getAll() {
        return ConfigConverter.convert(configRepository.getAll());
    }
}