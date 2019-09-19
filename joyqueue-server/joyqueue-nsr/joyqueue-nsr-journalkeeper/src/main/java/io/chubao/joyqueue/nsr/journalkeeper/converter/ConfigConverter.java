package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.nsr.journalkeeper.domain.ConfigDTO;
import org.apache.commons.collections.CollectionUtils;

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