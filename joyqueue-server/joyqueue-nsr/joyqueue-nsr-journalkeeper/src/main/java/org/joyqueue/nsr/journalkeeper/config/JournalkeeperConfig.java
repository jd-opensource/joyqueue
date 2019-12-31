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
package org.joyqueue.nsr.journalkeeper.config;

import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.helper.PortHelper;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
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
        return PortHelper.getJournalkeeperPort(propertySupplier.getValue(BrokerConfigKey.FRONTEND_SERVER_PORT));
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

    public int getSnapshotIntervalSec() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.SNAPSHOT_INTERVAL_SEC);
    }

    public int getJournalRetentionMin() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.JOURNAL_RETENTION_MIN_KEY);
    }

    public int getRpcTimeout() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.RPC_TIMEOUT);
    }

    public int getExecuteTimeout() {
        return PropertySupplier.getValue(propertySupplier, JournalkeeperConfigKey.EXECUTE_TIMEOUT);
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