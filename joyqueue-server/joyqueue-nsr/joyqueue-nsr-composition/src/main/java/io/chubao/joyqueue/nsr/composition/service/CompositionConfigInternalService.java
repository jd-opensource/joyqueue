package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;

import java.util.List;

/**
 * CompositionConfigInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConfigInternalService implements ConfigInternalService {

    private CompositionConfig config;
    private ConfigInternalService igniteConfigService;
    private ConfigInternalService journalkeeperConfigService;

    public CompositionConfigInternalService(CompositionConfig config, ConfigInternalService igniteConfigService,
                                            ConfigInternalService journalkeeperConfigService) {
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
