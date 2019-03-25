package com.jd.journalq.broker.coordinator;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.toolkit.service.Service;

import java.util.concurrent.ConcurrentMap;

/**
 * CoordinatorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorService extends Service{

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private NameService nameService;

    private CoordinatorInitializer coordinatorInitializer;
    private CoordinatorResolver coordinatorResolver;
    private Coordinator coordinator;

    private ConcurrentMap<String, CoordinatorGroupManager> coordinatorGroupManagerCache = Maps.newConcurrentMap();

    public CoordinatorService(CoordinatorConfig config, ClusterManager clusterManager, NameService nameService) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.nameService = nameService;
        this.coordinatorInitializer = new CoordinatorInitializer(config, clusterManager, nameService);
        this.coordinatorResolver = new CoordinatorResolver(config, clusterManager);
        this.coordinator = new Coordinator(config, clusterManager, coordinatorResolver, coordinatorInitializer);
    }

    @Override
    protected void doStart() throws Exception {
        coordinatorInitializer.init();
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public CoordinatorGroupManager getOrCreateCoordinatorGroupManager(String namespace) {
        CoordinatorGroupManager coordinatorGroupManager = coordinatorGroupManagerCache.get(namespace);
        if (coordinatorGroupManager == null) {
            coordinatorGroupManager = doCreateCoordinatorGroupManager(namespace);
            CoordinatorGroupManager oldCoordinatorGroupManager = coordinatorGroupManagerCache.putIfAbsent(namespace, coordinatorGroupManager);
            if (oldCoordinatorGroupManager != null) {
                coordinatorGroupManager = oldCoordinatorGroupManager;
            }
        }
        return coordinatorGroupManager;
    }

    protected CoordinatorGroupManager doCreateCoordinatorGroupManager(String namespace) {
        return new CoordinatorGroupManager(namespace, config);
    }
}