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
package org.joyqueue.nsr.journalkeeper.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.nsr.service.internal.ClusterInternalService;
import org.joyqueue.toolkit.network.IpUtil;
import io.journalkeeper.core.api.ClusterConfiguration;
import io.journalkeeper.core.api.ServerStatus;
import io.journalkeeper.sql.client.SQLClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
            Map<String, Object> result = Maps.newHashMap();
            ClusterConfiguration clusterConfiguration = sqlClient.getAdminClient().getClusterConfiguration().get();

            for (URI voter : clusterConfiguration.getVoters()) {
                if (voter.getHost().equals(IpUtil.getLocalIp())) {
                    ServerStatus serverStatus = sqlClient.getAdminClient().getServerStatus(voter).get();
                    result.put("status", serverStatus);
                }
            }

            result.put("cluster", clusterConfiguration);
            return JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect);
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

    @Override
    public String updateNodes(List<URI> uris) {
        try {
            ClusterConfiguration clusterConfiguration = sqlClient.getAdminClient().getClusterConfiguration().get();
            List<URI> oldConfig = clusterConfiguration.getVoters();
            List<URI> newConfig = uris;
            sqlClient.getAdminClient().updateVoters(oldConfig, newConfig);
            return String.format("{'oldConfig':'%s', 'newConfig':'%s'}", oldConfig, newConfig);
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }

    @Override
    public String execute(String command, List<String> args) {
        try {
            Object result = null;
            switch (command) {
                case "getSnapshots": {
                    result = sqlClient.getAdminClient().getSnapshots().get();
                    break;
                }
                case "takeSnapshot": {
                    sqlClient.getAdminClient().takeSnapshot().get();
                    result = "success";
                    break;
                }
                case "recoverSnapshot": {
                    sqlClient.getAdminClient().recoverSnapshot(Integer.valueOf(args.get(0))).get();
                    result = "success";
                    break;
                }
                default: {
                    result = "unsupported";
                    break;
                }
            }
            return JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect);
        } catch (Exception e) {
            throw new NsrException(e);
        }
    }
}