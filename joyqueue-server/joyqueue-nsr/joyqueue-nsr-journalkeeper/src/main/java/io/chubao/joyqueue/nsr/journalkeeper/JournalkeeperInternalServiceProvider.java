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
package io.chubao.joyqueue.nsr.journalkeeper;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.nsr.InternalServiceProvider;
import io.chubao.joyqueue.nsr.journalkeeper.config.JournalkeeperConfig;
import io.chubao.joyqueue.nsr.journalkeeper.config.JournalkeeperConfigKey;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.service.Service;
import io.journalkeeper.core.api.ClusterConfiguration;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.core.server.AbstractServer;
import io.journalkeeper.core.server.Server;
import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.SQLClientAccessPoint;
import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.client.support.DefaultSQLOperator;
import io.journalkeeper.sql.server.SQLServer;
import io.journalkeeper.sql.server.SQLServerAccessPoint;
import io.journalkeeper.sql.state.config.SQLConfigs;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * JournalkeeperInternalServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class JournalkeeperInternalServiceProvider extends Service implements InternalServiceProvider, PropertySupplierAware {

    protected static final Logger logger = LoggerFactory.getLogger(JournalkeeperInternalServiceProvider.class);

    private PropertySupplier propertySupplier;
    private JournalkeeperConfig config;

    private SQLServer sqlServer;
    private SQLClient sqlClient;
    private SQLOperator sqlOperator;
    private JournalkeeperInternalServiceManager journalkeeperInternalServiceManager;

    @Override
    public void setSupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.config = new JournalkeeperConfig(propertySupplier);
    }

    protected Properties convertProperties(JournalkeeperConfig config, List<Property> properties) {
        Properties result = new Properties();
        for (Property property : properties) {
            if (property.getKey().startsWith(JournalkeeperConfigKey.PREFIX.getName())) {
                result.setProperty(property.getKey().substring(JournalkeeperConfigKey.PREFIX.getName().length() + 1), property.getString());
            }
        }

        result.setProperty(AbstractServer.Config.SNAPSHOT_INTERVAL_SEC_KEY, String.valueOf(config.getSnapshotIntervalSec()));
        result.setProperty(AbstractServer.Config.JOURNAL_RETENTION_MIN_KEY, String.valueOf(config.getJournalRetentionMin()));
        result.setProperty(AbstractServer.Config.RPC_TIMEOUT_MS_KEY, String.valueOf(config.getRpcTimeout()));
        result.setProperty(AbstractServer.Config.FLUSH_INTERVAL_MS_KEY, String.valueOf(config.getFlushInterval()));
        result.setProperty(AbstractServer.Config.WORKING_DIR_KEY, String.valueOf(config.getWorkingDir()));
        result.setProperty(AbstractServer.Config.GET_STATE_BATCH_SIZE_KEY, String.valueOf(config.getStateBatchSize()));
        result.setProperty(AbstractServer.Config.ENABLE_METRIC_KEY, String.valueOf(config.getMetricEnable()));
        result.setProperty(AbstractServer.Config.PRINT_METRIC_INTERVAL_SEC_KEY, String.valueOf(config.getMetricPrintInterval()));
        result.setProperty(SQLConfigs.INIT_FILE, config.getInitFile());
        return result;
    }

    @Override
    protected void doStart() throws Exception {
        Properties journalkeeperProperties = convertProperties(config, propertySupplier.getProperties());
        URI currentNode = URI.create(String.format("journalkeeper://%s:%s", config.getLocal(), config.getPort()));
        List<URI> nodes = parseNodeUris(currentNode, config.getNodes());

        if (Server.Roll.VOTER.name().equals(config.getRole())
                || RaftServer.Roll.OBSERVER.name().equals(config.getRole())) {

            Server.Roll role = Server.Roll.valueOf(config.getRole());
            SQLServerAccessPoint serverAccessPoint = new SQLServerAccessPoint(journalkeeperProperties);

            if (CollectionUtils.isNotEmpty(nodes) && !nodes.contains(currentNode)) {
                sqlServer = serverAccessPoint.createServer(currentNode, nodes, role);

                if (!sqlServer.isInitialized()) {
                    logger.info("journalkeeper cluster not initialized, current: {}, nodes: {}", currentNode, nodes);
                    sqlServer.tryStart();
                    sqlServer.waitClusterReady(config.getWaitLeaderTimeout(), TimeUnit.MILLISECONDS);

                    ClusterConfiguration clusterConfiguration = sqlServer.getAdminClient().getClusterConfiguration().get();
                    logger.info("get journalkeeper cluster, leader: {}, voters: {}", clusterConfiguration.getLeader(), clusterConfiguration.getVoters());

                    List<URI> currentVoters = clusterConfiguration.getVoters();
                    if (!currentVoters.contains(currentNode)) {
                        List<URI> newVoters = Lists.newArrayList(currentVoters);
                        newVoters.add(currentNode);
                        logger.info("update journalkeeper cluster, oldVoters: {}, newVoters: {}", currentVoters, newVoters);
                        sqlServer.getAdminClient().updateVoters(currentVoters, newVoters).get();
                    }
                } else {
                    sqlServer.tryStart();
                }
            } else {
                if (CollectionUtils.isEmpty(nodes)) {
                    nodes.add(currentNode);
                }
                sqlServer = serverAccessPoint.createServer(currentNode, nodes, role);
                sqlServer.tryStart();
            }

            sqlServer.waitClusterReady(config.getWaitLeaderTimeout(), TimeUnit.MILLISECONDS);
            this.sqlClient = this.sqlServer.getClient();
        } else {
            SQLClientAccessPoint clientAccessPoint = new SQLClientAccessPoint(journalkeeperProperties);
            this.sqlClient = clientAccessPoint.createClient(nodes);
        }
        this.sqlOperator = new DefaultSQLOperator(this.sqlClient);
        BatchOperationContext.init(sqlOperator);
        this.journalkeeperInternalServiceManager = new JournalkeeperInternalServiceManager(this.sqlServer, this.sqlClient, this.sqlOperator);
        this.journalkeeperInternalServiceManager.start();
    }

    protected static List<URI> parseNodeUris(URI currentNode, List<String> nodes) {
        List<URI> nodesUri = Lists.newArrayList();
        for (String node : nodes) {
            String[] split = node.split(":");
            nodesUri.add(URI.create(String.format("journalkeeper://%s:%s", split[0], split[1])));
        }
        return nodesUri;
    }

    @Override
    protected void doStop() {
        if (sqlServer != null) {
            sqlServer.stop();
        }
        if (sqlClient != null) {
            sqlClient.stop();
        }
        if (journalkeeperInternalServiceManager != null) {
            journalkeeperInternalServiceManager.stop();
        }
    }

    @Override
    public <T> T getService(Class<T> service) {
        return journalkeeperInternalServiceManager.getService(service);
    }

    @Override
    public String type() {
        return JournalkeeperConsts.TYPE;
    }
}