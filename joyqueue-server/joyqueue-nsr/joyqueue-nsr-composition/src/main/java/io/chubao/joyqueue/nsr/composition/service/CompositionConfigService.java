package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
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
    public Config getById(String id) {
        return igniteConfigService.getById(id);
    }

    @Override
    public Config getByGroupAndKey(String group, String key) {
        return igniteConfigService.getByGroupAndKey(group, key);
    }

    @Override
    public List<Config> getAll() {
        return igniteConfigService.getAll();
    }

    @Override
    public Config add(Config config) {
        return igniteConfigService.add(config);
    }

    @Override
    public Config update(Config config) {
        return igniteConfigService.update(config);
    }

    @Override
    public void delete(String id) {
        igniteConfigService.delete(id);
    }
}
