package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ConfigConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConfigRepository;
import io.chubao.joyqueue.nsr.model.ConfigQuery;
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
    public Config getByGroupAndKey(String group, String key) {
        return ConfigConverter.convert(configRepository.getByKeyAndGroup(key, group));
    }

    @Override
    public void add(Config config) {
        configRepository.add(ConfigConverter.convert(config));
    }

    @Override
    public void update(Config config) {

    }

    @Override
    public void remove(Config config) {

    }

    @Override
    public Config getById(String id) {
        return null;
    }

    @Override
    public Config get(Config model) {
        return null;
    }

    @Override
    public void addOrUpdate(Config config) {

    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(Config model) {

    }

    @Override
    public List<Config> list() {
        return null;
    }

    @Override
    public List<Config> list(ConfigQuery query) {
        return null;
    }

    @Override
    public PageResult<Config> pageQuery(QPageQuery<ConfigQuery> pageQuery) {
        return null;
    }
}