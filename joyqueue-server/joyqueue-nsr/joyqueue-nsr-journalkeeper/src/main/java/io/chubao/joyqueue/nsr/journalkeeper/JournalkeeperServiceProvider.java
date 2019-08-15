package io.chubao.joyqueue.nsr.journalkeeper;

import io.chubao.joyqueue.nsr.ServiceProvider;
import io.chubao.joyqueue.nsr.journalkeeper.config.JournalkeeperConfig;
import io.chubao.joyqueue.nsr.journalkeeper.config.JournalkeeperConfigKey;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.service.Service;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.core.server.Server;
import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.server.SQLServer;

import java.util.Properties;

/**
 * JournalkeeperServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class JournalkeeperServiceProvider extends Service implements ServiceProvider, PropertySupplierAware {

    private PropertySupplier propertySupplier;
    private JournalkeeperConfig config;

    private SQLServer sqlServer;
    private SQLClient sqlClient;
    private SQLOperator sqlOperator;

    @Override
    public void setSupplier(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.config = new JournalkeeperConfig(propertySupplier);
    }

    @Override
    protected void validate() throws Exception {
        Properties journalkeeperProperties = new Properties();
        for (Property property : propertySupplier.getProperties()) {
            if (property.getName().startsWith(JournalkeeperConfigKey.PREFIX.getName())) {
                journalkeeperProperties.setProperty(property.getName().substring(JournalkeeperConfigKey.PREFIX.getName().length() + 1), property.getString());
            }
        }

        if (Server.Roll.VOTER.name().equals(config.getRole())
                || RaftServer.Roll.OBSERVER.name().equals(config.getRole())) {
            Server.Roll role = Server.Roll.valueOf(config.getRole());

        } else {

        }
    }

    @Override
    protected void doStart() throws Exception {
        if (sqlServer != null) {
            sqlServer.start();
        }
    }

    @Override
    protected void doStop() {
        if (sqlServer != null) {
            sqlServer.stop();
        }
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        return null;
    }
}