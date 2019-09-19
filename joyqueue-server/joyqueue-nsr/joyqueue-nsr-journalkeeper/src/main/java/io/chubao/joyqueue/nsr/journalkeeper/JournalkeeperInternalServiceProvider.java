package io.chubao.joyqueue.nsr.journalkeeper;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.nsr.InternalServiceProvider;
import io.chubao.joyqueue.nsr.journalkeeper.config.JournalkeeperConfig;
import io.chubao.joyqueue.nsr.journalkeeper.config.JournalkeeperConfigKey;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.service.Service;
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

    @Override
    protected void validate() throws Exception {
        Properties journalkeeperProperties = convertProperties(config, propertySupplier.getProperties());
        List<URI> nodes = convertNodeUri(config.getLocal(), config.getNodes(), config.getPort());

        logger.info("nodes: {}", nodes);

        if (Server.Roll.VOTER.name().equals(config.getRole())
                || RaftServer.Roll.OBSERVER.name().equals(config.getRole())) {

            Server.Roll role = Server.Roll.valueOf(config.getRole());
            SQLServerAccessPoint serverAccessPoint = new SQLServerAccessPoint(journalkeeperProperties);
            this.sqlServer = serverAccessPoint.createServer(nodes.get(0), nodes, role);
            this.sqlClient = this.sqlServer.getClient();
        } else {
            SQLClientAccessPoint clientAccessPoint = new SQLClientAccessPoint(journalkeeperProperties);
            this.sqlClient = clientAccessPoint.createClient(nodes);
        }
        this.sqlOperator = new DefaultSQLOperator(this.sqlClient);
        TransactionContext.init(sqlOperator);
        this.journalkeeperInternalServiceManager = new JournalkeeperInternalServiceManager(this.sqlClient, this.sqlOperator);
    }

    protected Properties convertProperties(JournalkeeperConfig config, List<Property> properties) {
        Properties result = new Properties();
        for (Property property : properties) {
            if (property.getKey().startsWith(JournalkeeperConfigKey.PREFIX.getName())) {
                result.setProperty(property.getKey().substring(JournalkeeperConfigKey.PREFIX.getName().length() + 1), property.getString());
            }
        }

        result.setProperty(AbstractServer.Config.SNAPSHOT_STEP_KEY, String.valueOf(config.getSnapshotStep()));
        result.setProperty(AbstractServer.Config.RPC_TIMEOUT_MS_KEY, String.valueOf(config.getRpcTimeout()));
        result.setProperty(AbstractServer.Config.FLUSH_INTERVAL_MS_KEY, String.valueOf(config.getFlushInterval()));
        result.setProperty(AbstractServer.Config.WORKING_DIR_KEY, String.valueOf(config.getWorkingDir()));
        result.setProperty(AbstractServer.Config.GET_STATE_BATCH_SIZE_KEY, String.valueOf(config.getStateBatchSize()));
        result.setProperty(AbstractServer.Config.ENABLE_METRIC_KEY, String.valueOf(config.getMetricEnable()));
        result.setProperty(AbstractServer.Config.PRINT_METRIC_INTERVAL_SEC_KEY, String.valueOf(config.getMetricPrintInterval()));
        result.setProperty(SQLConfigs.INIT_FILE, config.getInitFile());
        return result;
    }

    protected List<URI> convertNodeUri(String local, List<String> nodes, int port) {
        List<URI> nodesUri = Lists.newArrayList();
        nodesUri.add(URI.create(String.format("journalkeeper://%s:%s", local, port)));
        for (String node : nodes) {
            if (local.equals(node)) {
                continue;
            }
            nodesUri.add(URI.create(String.format("journalkeeper://%s:%s", node, port)));
        }
        return nodesUri;
    }

    @Override
    protected void doStart() throws Exception {
        if (sqlServer != null) {
            sqlServer.start();
            sqlServer.waitForLeaderReady(config.getWaitLeaderTimeout(), TimeUnit.MILLISECONDS);
        }
        if (journalkeeperInternalServiceManager != null) {
            journalkeeperInternalServiceManager.start();
        }
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