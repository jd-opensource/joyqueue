package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionConfigInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConfigInternalService implements ConfigInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionConfigInternalService.class);

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
        Config result = igniteConfigService.add(config);
        if (this.config.isWriteJournalkeeper()) {
            try {
                journalkeeperConfigService.add(config);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", config, e);
            }
        }
        return result;
    }

    @Override
    public Config update(Config config) {
        Config result = igniteConfigService.update(config);
        if (this.config.isWriteJournalkeeper()) {
            try {
                journalkeeperConfigService.update(config);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", config, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        igniteConfigService.delete(id);
        if (this.config.isWriteJournalkeeper()) {
            try {
                journalkeeperConfigService.delete(id);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
