/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.nameservice;

import com.alibaba.fastjson.JSON;
import org.joyqueue.domain.AllMetadata;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CompensateMetadataThread
 * author: gaohaoxiang
 * date: 2019/9/2
 */
public class CompensateMetadataThread extends Service implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(CompensateMetadataThread.class);

    private NameServiceConfig config;
    private NameService delegate;
    private MetadataCacheManager metadataCacheManager;
    private MetadataCompensator metadataCompensator;

    private MetadataValidator metadataValidator;
    private Thread compensationThread;
    private volatile boolean started = false;

    public CompensateMetadataThread(NameServiceConfig config, NameService delegate,
                                    MetadataCacheManager metadataCacheManager, MetadataCompensator metadataCompensator) {
        this.config = config;
        this.delegate = delegate;
        this.metadataCacheManager = metadataCacheManager;
        this.metadataCompensator = metadataCompensator;
    }

    @Override
    protected void validate() throws Exception {
        metadataValidator = new MetadataValidator(config);
        compensationThread = new Thread(this, "joyqueue-metadata-compensation");
        compensationThread.setDaemon(true);
    }

    @Override
    protected void doStart() throws Exception {
        started = true;
        compensationThread.start();
    }

    @Override
    protected void doStop() {
        started = false;
    }

    @Override
    public void run() {
        while (started) {
            try {
                doCompensate();
            } catch (Exception e) {
                logger.error("compensate exception", e);
            }
            try {
                Thread.currentThread().sleep(config.getCompensationInterval());
            } catch (InterruptedException e) {
            }
        }
    }

    public void doCompensate() {
        if (!config.getCompensationEnable()) {
            return;
        }

        if (metadataCacheManager.getCache() == null) {
            AllMetadata allMetadata = delegate.getAllMetadata();
            AllMetadataCache newCache = metadataCacheManager.buildCache(allMetadata);

            if (logger.isDebugEnabled()) {
                logger.debug("doCompensate, newCache: {}, metadata: {}", JSON.toJSONString(newCache), JSON.toJSONString(allMetadata));
            }

            metadataCacheManager.fillCache(newCache);
            metadataCacheManager.flushCache();
        } else {
            if (metadataCompensator.getBrokerId() < 0) {
                return;
            }
            if (!metadataCacheManager.tryLock()) {
                return;
            }
            try {
                boolean isFlush = true;
                AllMetadata allMetadata = null;
                AllMetadataCache newCache = null;
                AllMetadataCache oldCache = metadataCacheManager.getCache();
                long version = metadataCacheManager.getTimestamp();

                if (SystemClock.now() - version < config.getAllMetadataInterval()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("doCompensate, interval less than threshold, last: {}, threshold: {}",
                                version, config.getAllMetadataInterval());
                    }
                    return;
                }

                for (int i = 0; i <= config.getCompensationRetryTimes(); i++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("doCompensate pre, oldCache: {}", JSON.toJSONString(oldCache));
                    }

                    try {
                        allMetadata = delegate.getAllMetadata();
                    } catch (Exception e) {
                        logger.error("getAllMetadata exception", e);
                        continue;
                    }

                    newCache = metadataCacheManager.buildCache(allMetadata);

                    if (logger.isDebugEnabled()) {
                        logger.debug("doCompensate, oldCache: {}, newCache: {}, metadata: {}",
                                JSON.toJSONString(oldCache), JSON.toJSONString(newCache), JSON.toJSONString(allMetadata));
                    }

                    if (!metadataValidator.validateChange(oldCache, newCache)) {
                        logger.error("doCompensate validate error");
                        if (logger.isDebugEnabled()) {
                            logger.debug("doCompensate validate error, oldCache: {}, newCache: {}", oldCache, newCache);
                        }
                        isFlush = false;
                        break;
                    }

                    if (config.getCompensationEnable()) {
                        metadataCompensator.compensate(oldCache, newCache);
                    }

                    long currentVersion = metadataCacheManager.getTimestamp();
                    if (version == currentVersion) {
                        break;
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("doCompensate retry, oldVersion: {}, currentVersion: {}", version, currentVersion);
                        }
                        version = currentVersion;
                        oldCache = newCache;
                        try {
                            Thread.currentThread().sleep(config.getCompensationRetryInterval());
                        } catch (Exception e) {
                        }
                    }
                }

                if (newCache != null && isFlush) {
                    metadataCacheManager.fillCache(newCache);
                    metadataCacheManager.flushCache();
                }
            } finally {
                metadataCacheManager.unlock();
            }
        }
    }
}