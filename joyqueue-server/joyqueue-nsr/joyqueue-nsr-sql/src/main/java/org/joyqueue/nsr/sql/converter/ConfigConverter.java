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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.Config;
import org.joyqueue.nsr.sql.domain.ConfigDTO;

import java.util.Collections;
import java.util.List;

/**
 * ConfigConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConfigConverter {

    public static ConfigDTO convert(Config config) {
        if (config == null) {
            return null;
        }
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setId(generateId(config));
        configDTO.setGroup(config.getGroup());
        configDTO.setKey(config.getKey());
        configDTO.setValue(config.getValue());
        return configDTO;
    }

    protected static String generateId(Config config) {
        return String.format("%s.%s", config.getGroup(), config.getKey());
    }

    public static Config convert(ConfigDTO configDTO) {
        if (configDTO == null) {
            return null;
        }
        Config config = new Config();
        config.setKey(configDTO.getKey());
        config.setValue(configDTO.getValue());
        config.setGroup(configDTO.getGroup());
        return config;
    }

    public static List<Config> convert(List<ConfigDTO> configDTOList) {
        if (CollectionUtils.isEmpty(configDTOList)) {
            return Collections.emptyList();
        }
        List<Config> result = Lists.newArrayListWithCapacity(configDTOList.size());
        for (ConfigDTO configDTO : configDTOList) {
            result.add(convert(configDTO));
        }
        return result;
    }
}