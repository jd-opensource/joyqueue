package io.chubao.joyqueue.nsr.journalkeeper.config;

import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * JournalkeeperConfig
 * author: gaohaoxiang
 * date: 2019/8/14
 */
public class JournalkeeperConfig {

    protected static final Logger logger = LoggerFactory.getLogger(JournalkeeperConfig.class);

    private PropertySupplier propertySupplier;

    public JournalkeeperConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getPort() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.PORT);
    }

    public String getRole() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.ROLE);
    }

    public String getLocal() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.LOCAL);
    }

    public List<String> getNodes() {
        String nodes = PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.NODES);
        if (StringUtils.isBlank(nodes)) {
            return Collections.emptyList();
        }
        return Arrays.asList(nodes.split(JournalkeeperConfigKey.NODE_SPLITTER));
    }

    public int getWaitLeaderTimeout() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.WAIT_LEADER_TIMEOUT);
    }

    public String getWorkingDir() {
        String dir = PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.WORKING_DIR);
        if (StringUtils.isNotBlank(dir)) {
            return dir;
        }
        return propertySupplier.getProperty(Property.APPLICATION_DATA_PATH).getString() + JournalkeeperConfigKey.DEFAULT_WORKING_DIR;
    }

    public String getInitFile() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.INIT_FILE);
    }

    public int getSnapshotStep() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.SNAPSHOT_STEP);
    }

    public int getRpcTimeout() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.RPC_TIMEOUT);
    }

    public int getFlushInterval() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.FLUSH_INTERVAL);
    }

    public int getStateBatchSize() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.STATE_BATCH_SIZE);
    }

    public boolean getMetricEnable() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.METRIC_ENABLE);
    }

    public int getMetricPrintInterval() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.METRIC_PRINT_INTERVAL);
    }
}