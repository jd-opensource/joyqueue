package io.chubao.joyqueue.nsr.journalkeeper.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.nsr.service.internal.ClusterInternalService;
import io.journalkeeper.core.api.ClusterConfiguration;
import io.journalkeeper.sql.client.SQLClient;

import java.net.URI;
import java.util.List;

/**
 * JournalkeeperClusterInternalService
 * author: gaohaoxiang
 * date: 2019/10/31
 */
public class JournalkeeperClusterInternalService implements ClusterInternalService {

    private SQLClient sqlClient;

    public JournalkeeperClusterInternalService(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @Override
    public String getCluster() {
        try {
            return JSON.toJSONString(sqlClient.getAdminClient().getClusterConfiguration().get());
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }

    @Override
    public String addNode(URI uri) {
        try {
            ClusterConfiguration clusterConfiguration = sqlClient.getAdminClient().getClusterConfiguration().get();
            List<URI> oldConfig = clusterConfiguration.getVoters();
            List<URI> newConfig = Lists.newArrayList(oldConfig);
            newConfig.add(uri);
            sqlClient.getAdminClient().updateVoters(oldConfig, newConfig);
            return String.format("{'oldConfig':'%s', 'newConfig':'%s'}", oldConfig, newConfig);
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }

    @Override
    public String removeNode(URI uri) {
        try {
            ClusterConfiguration clusterConfiguration = sqlClient.getAdminClient().getClusterConfiguration().get();
            List<URI> oldConfig = clusterConfiguration.getVoters();
            List<URI> newConfig = Lists.newArrayList(oldConfig);
            newConfig.remove(uri);
            sqlClient.getAdminClient().updateVoters(oldConfig, newConfig);
            return String.format("{'oldConfig':'%s', 'newConfig':'%s'}", oldConfig, newConfig);
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }
}