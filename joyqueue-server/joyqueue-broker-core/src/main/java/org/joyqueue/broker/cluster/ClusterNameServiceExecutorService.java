package org.joyqueue.broker.cluster;

import com.google.common.collect.Maps;
import org.joyqueue.broker.cluster.config.ClusterConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClusterNameServiceExecutorService
 * author: gaohaoxiang
 * date: 2020/4/1
 */
public class ClusterNameServiceExecutorService extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ClusterNameServiceExecutorService.class);

    private ClusterConfig config;
    private ExecutorService executorService;
    private ConcurrentMap<TopicName, Queue<Runnable>> taskMap = Maps.newConcurrentMap();

    public ClusterNameServiceExecutorService(ClusterConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        this.executorService = new ThreadPoolExecutor(config.getTopicDynamicMetadataBatchMinThreads(), config.getTopicDynamicMetadataBatchMaxThreads(),
                config.getTopicDynamicMetadataBatchKeepalive(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(config.getTopicDynamicMetadataBatchQueueSize()),
                new NamedThreadFactory("joyqueue-cluster-nameservice-threads"), new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    protected void doStop() {
        executorService.shutdownNow();
    }

    public void execute(Runnable runnable) {
        this.executorService.execute(runnable);
    }
}