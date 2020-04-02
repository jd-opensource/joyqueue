package org.joyqueue.nsr.nameservice;

import org.joyqueue.nsr.config.NameServiceConfig;

/**
 * MetadataValidator
 * author: gaohaoxiang
 * date: 2020/3/30
 */
public class MetadataValidator {

    private NameServiceConfig config;

    public MetadataValidator(NameServiceConfig config) {
        this.config = config;
    }

    public boolean validateChange(AllMetadataCache oldCache, AllMetadataCache newCache) {
        if (oldCache.getAllTopicConfigs().size() - newCache.getAllTopicConfigs().size() > config.getCompensationThreshold()) {
            return false;
        }
        if (oldCache.getAllConsumers().size() - newCache.getAllConsumers().size() > config.getCompensationThreshold()) {
            return false;
        }
        if (oldCache.getAllProducers().size() - newCache.getAllProducers().size() > config.getCompensationThreshold()) {
            return false;
        }
        return true;
    }
}