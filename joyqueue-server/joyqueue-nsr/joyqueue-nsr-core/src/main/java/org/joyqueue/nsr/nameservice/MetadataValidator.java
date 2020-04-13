package org.joyqueue.nsr.nameservice;

import org.joyqueue.nsr.config.NameServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetadataValidator
 * author: gaohaoxiang
 * date: 2020/3/30
 */
public class MetadataValidator {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataValidator.class);

    private NameServiceConfig config;

    public MetadataValidator(NameServiceConfig config) {
        this.config = config;
    }

    public boolean validateChange(AllMetadataCache oldCache, AllMetadataCache newCache) {
        if (oldCache.getAllTopicConfigs().size() - newCache.getAllTopicConfigs().size() > config.getCompensationThreshold()) {
            logger.error("validate change error, oldTopic: {}, newTopic: {}, threshold: {}",
                    oldCache.getAllTopicConfigs().size(), newCache.getAllTopicConfigs().size(), config.getCompensationThreshold());
            return false;
        }
        if (oldCache.getAllConsumers().size() - newCache.getAllConsumers().size() > config.getCompensationThreshold()) {
            logger.error("validate change error, oldConsumer: {}, newConsumer: {}, threshold: {}",
                    oldCache.getAllConsumers().size(), newCache.getAllConsumers().size(), config.getCompensationThreshold());
            return false;
        }
        if (oldCache.getAllProducers().size() - newCache.getAllProducers().size() > config.getCompensationThreshold()) {
            logger.error("validate change error, oldProducer: {}, oldConsumer: {}, threshold: {}",
                    oldCache.getAllProducers().size(), newCache.getAllProducers().size(), config.getCompensationThreshold());
            return false;
        }
        return true;
    }
}