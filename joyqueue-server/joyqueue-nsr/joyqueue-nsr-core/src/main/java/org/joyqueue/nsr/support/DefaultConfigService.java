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

import org.joyqueue.domain.Config;
import org.joyqueue.nsr.service.ConfigService;
import org.joyqueue.nsr.service.internal.ConfigInternalService;

import java.util.List;

/**
 * DefaultConfigService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultConfigService implements ConfigService {

    private ConfigInternalService configInternalService;

    public DefaultConfigService(ConfigInternalService configInternalService) {
        this.configInternalService = configInternalService;
    }

    @Override
    public Config getById(String id) {
        return configInternalService.getById(id);
    }

    @Override
    public Config getByGroupAndKey(String group, String key) {
        return configInternalService.getByGroupAndKey(group, key);
    }

    @Override
    public List<Config> getAll() {
        return configInternalService.getAll();
    }

    @Override
    public Config add(Config config) {
        return configInternalService.add(config);
    }

    @Override
    public Config update(Config config) {
        return configInternalService.update(config);
    }

    @Override
    public void delete(String id) {
        configInternalService.delete(id);
    }
}