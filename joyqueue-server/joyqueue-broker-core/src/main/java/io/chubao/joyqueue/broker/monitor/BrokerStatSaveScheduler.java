package io.chubao.joyqueue.broker.monitor;

import io.chubao.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import io.chubao.joyqueue.toolkit.concurrent.NamedThreadFactory;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * brokerstat保存调度
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public class BrokerStatSaveScheduler extends Service implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerStatSaveScheduler.class);

    private BrokerMonitorConfig config;
    private BrokerStatManager brokerStatManager;
    private ScheduledExecutorService executorService;

    public BrokerStatSaveScheduler(BrokerMonitorConfig config, BrokerStatManager brokerStatManager) {
        this.config = config;
        this.brokerStatManager = brokerStatManager;
        this.executorService = newScheduledExecutorService();
    }

    @Override
    protected void doStart() throws Exception {
        this.executorService.scheduleWithFixedDelay(this, config.getStatSaveInterval(), config.getStatSaveInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        this.executorService.shutdown();
    }

    @Override
    public void run() {
        try {
            brokerStatManager.save();
        } catch (Exception e) {
            logger.error("save broker stat exception", e);
        }
    }

    protected ScheduledExecutorService newScheduledExecutorService() {
        NamedThreadFactory threadFactory = new NamedThreadFactory("joyqueue-stat-save-scheduler");
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
}