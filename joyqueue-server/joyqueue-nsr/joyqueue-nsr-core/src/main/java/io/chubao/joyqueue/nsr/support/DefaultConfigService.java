package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.nsr.service.ConfigService;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;

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