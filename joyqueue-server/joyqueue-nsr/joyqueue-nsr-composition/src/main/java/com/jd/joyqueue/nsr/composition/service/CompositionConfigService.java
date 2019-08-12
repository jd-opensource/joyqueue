package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.domain.Config;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.nsr.model.ConfigQuery;
import com.jd.joyqueue.nsr.service.ConfigService;

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
