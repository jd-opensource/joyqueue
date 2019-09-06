package io.chubao.joyqueue.nsr.nameservice;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.domain.AllMetadata;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NameServiceCompensateThread
 * author: gaohaoxiang
 * date: 2019/9/2
 */
public class NameServiceCompensateThread extends Service implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(NameServiceCompensateThread.class);

    private NameServiceConfig config;
    private NameService delegate;
    private NameServiceCacheManager nameServiceCacheManager;
    private NameServiceCompensator nameServiceCompensator;

    private Thread compensationThread;
    private volatile boolean started = false;

    public NameServiceCompensateThread(NameServiceConfig config, NameService delegate,
                                       NameServiceCacheManager nameServiceCacheManager, NameServiceCompensator nameServiceCompensator) {
        this.config = config;
        this.delegate = delegate;
        this.nameServiceCacheManager = nameServiceCacheManager;
        this.nameServiceCompensator = nameServiceCompensator;
    }

    @Override
    protected void validate() throws Exception {
        compensationThread = new Thread(this, "joyqueue-nameservice-compensation");
    }

    @Override
    protected void doStart() throws Exception {
        doFillCache();
        started = true;
        compensationThread.start();
    }

    @Override
    protected void doStop() {
        started = false;
        compensationThread.interrupt();
    }

    @Override
    public void run() {
        while (started) {
            try {
                doCompensate();
                Thread.currentThread().sleep(config.getCompensationInterval());
            } catch (Exception e) {
                logger.error("compensate exception", e);
            }
        }
    }

    protected void doFillCache() {
        AllMetadata allMetadata = delegate.getAllMetadata();
        nameServiceCacheManager.fillCache(allMetadata);
    }

    protected void doCompensate() {
        nameServiceCacheManager.getLock().lock();
        try {
            NameServiceCache oldCache = nameServiceCacheManager.getCache();

            if (oldCache.isLatest(config.getCompensationInterval())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("doCompensate, cache is latest, oldCache: {}", JSON.toJSONString(oldCache));
                }
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("doCompensate pre, oldCache: {}", JSON.toJSONString(oldCache));
            }

            AllMetadata allMetadata = delegate.getAllMetadata();
            NameServiceCache newCache = nameServiceCacheManager.buildCache(allMetadata);

            if (logger.isDebugEnabled()) {
                logger.debug("doCompensate, oldCache: {}, newCache: {}, metadata: {}",
                        JSON.toJSONString(oldCache), JSON.toJSONString(newCache), JSON.toJSONString(allMetadata));
            }

            nameServiceCompensator.compensate(oldCache, newCache);
            nameServiceCacheManager.fillCache(newCache);
        } finally {
            nameServiceCacheManager.getLock().unlock();
        }
    }
}