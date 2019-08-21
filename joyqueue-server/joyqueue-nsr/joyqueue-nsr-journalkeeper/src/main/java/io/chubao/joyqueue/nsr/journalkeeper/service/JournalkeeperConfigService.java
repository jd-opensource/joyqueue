package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ConfigConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConfigRepository;
import io.chubao.joyqueue.nsr.service.ConfigService;

import java.util.List;

/**
 * JournalkeeperConfigService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperConfigService implements ConfigService {

    private ConfigRepository configRepository;

    public JournalkeeperConfigService(ConfigRepository configRepository) {
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