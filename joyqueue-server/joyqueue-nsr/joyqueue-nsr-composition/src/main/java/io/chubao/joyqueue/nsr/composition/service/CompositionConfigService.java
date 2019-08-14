package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.model.ConfigQuery;
import io.chubao.joyqueue.nsr.service.ConfigService;

import java.util.List;

/**
 * CompositionConfigService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConfigService implements ConfigService {

    private CompositionConfig config;
    private ConfigService igniteConfigService;
    private ConfigService journalkeeperConfigService;

    public CompositionConfigService(CompositionConfig config, ConfigService igniteConfigService,
                                    ConfigService journalkeeperConfigService) {
        this.config = config;
        this.igniteConfigService = igniteConfigService;
        this.journalkeeperConfigService = journalkeeperConfigService;
    }

    @Override
    public Config getByGroupAndKey(String group, String key) {
        return igniteConfigService.getByGroupAndKey(group, key);
    }

    @Override
    public void add(Config config) {
        igniteConfigService.add(config);
    }

    @Override
    public void update(Config config) {
        igniteConfigService.update(config);
    }

    @Override
    public void remove(Config config) {
        igniteConfigService.remove(config);
    }

    @Override
    public Config getById(String id) {
        return igniteConfigService.getById(id);
    }

    @Override
    public Config get(Config model) {
        return igniteConfigService.get(model);
    }

    @Override
    public void addOrUpdate(Config config) {
        igniteConfigService.addOrUpdate(config);
    }

    @Override
    public void deleteById(String id) {
        igniteConfigService.deleteById(id);
    }

    @Override
    public void delete(Config model) {
        igniteConfigService.delete(model);
    }

    @Override
    public List<Config> list() {
        return igniteConfigService.list();
    }

    @Override
    public List<Config> list(ConfigQuery query) {
        return igniteConfigService.list(query);
    }

    @Override
    public PageResult<Config> pageQuery(QPageQuery<ConfigQuery> pageQuery) {
        return igniteConfigService.pageQuery(pageQuery);
    }
}
