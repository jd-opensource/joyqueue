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
package org.joyqueue.broker.election;

import org.joyqueue.broker.config.Configuration;
import org.junit.Assert;
import org.junit.Test;


public class ElectionConfigTest {
    private Configuration conf = new Configuration();
    private ElectionConfig electionConfig = new ElectionConfig(conf);

    @Test
    public void testElectionConfig() {
        long transferLeaderMinLag = electionConfig.getTransferLeaderMinLag();
        Assert.assertEquals(transferLeaderMinLag, ElectionConfigKey.TRANSFER_LEADER_MIN_LAG.getValue());

        int commandQueueSize = electionConfig.getCommandQueueSize();
        Assert.assertEquals(commandQueueSize, ElectionConfigKey.COMMAND_QUEUE_SIZE.getValue());

        int disableStoreTimeout = electionConfig.getDisableStoreTimeout();
        Assert.assertEquals(disableStoreTimeout, ElectionConfigKey.DISABLE_STORE_TIMEOUT.getValue());

        int electionTimeout = electionConfig.getElectionTimeout();
        Assert.assertEquals(electionTimeout, ElectionConfigKey.ELECTION_TIMEOUT.getValue());

        int executorThreadNumMax = electionConfig.getExecutorThreadNumMax();
        Assert.assertEquals(executorThreadNumMax, ElectionConfigKey.EXECUTOR_THREAD_NUM_MAX.getValue());

        int executorThreadNumMin = electionConfig.getExecutorThreadNumMin();
        Assert.assertEquals(executorThreadNumMin, ElectionConfigKey.EXECUTOR_THREAD_NUM_MIN.getValue());

        int heartbeatTimeout = electionConfig.getHeartbeatTimeout();
        Assert.assertEquals(heartbeatTimeout, ElectionConfigKey.HEARTBEAT_TIMEOUT.getValue());

        electionConfig.setListenPort("10000");
        int listenPort = electionConfig.getListenPort();
        Assert.assertEquals(listenPort, 10000);

        int logInterval = electionConfig.getLogInterval();
        Assert.assertEquals(logInterval, ElectionConfigKey.LOG_INTERVAL.getValue());

        int maxReplicateLength = electionConfig.getMaxReplicateLength();
        Assert.assertEquals(maxReplicateLength, electionConfig.getMaxReplicateLength());

        boolean enableReportLeaderPeriodically = electionConfig.enableReportLeaderPeriodically();
        Assert.assertEquals(enableReportLeaderPeriodically, ElectionConfigKey.ENABLE_REPORT_LEADER_PERIODICALLY.getValue());

        boolean enableRebalanceLeader = electionConfig.enableRebalanceLeader();
        Assert.assertEquals(enableRebalanceLeader, ElectionConfigKey.ENABLE_REBALANCE_LEADER.getValue());

        int minRebalanceLeaderInterval = electionConfig.getMinRebalanceLeaderInterval();
        Assert.assertEquals(minRebalanceLeaderInterval, ElectionConfigKey.MIN_REBALANCE_INTERVAL.getValue());

        int replicateConsumePosInterval = electionConfig.getReplicateConsumePosInterval();
        Assert.assertEquals(replicateConsumePosInterval, ElectionConfigKey.REPLICATE_CONSUME_POS_INTERVAL.getValue());

        int minReplicateThreadNum = electionConfig.getReplicateThreadNumMin();
        Assert.assertEquals(minReplicateThreadNum, ElectionConfigKey.REPLICATE_THREAD_NUM_MIN.getValue());

        int maxReplicateThreadNum = electionConfig.getReplicateThreadNumMax();
        Assert.assertEquals(maxReplicateThreadNum, ElectionConfigKey.REPLICATE_THREAD_NUM_MAX.getValue());

        int sendCommandTimeout = electionConfig.getSendCommandTimeout();
        Assert.assertEquals(sendCommandTimeout, ElectionConfigKey.SEND_COMMAND_TIMEOUT.getValue());

        int timerScheduleThreadNum = electionConfig.getTimerScheduleThreadNum();
        Assert.assertEquals(timerScheduleThreadNum, ElectionConfigKey.TIMER_SCHEDULE_THREAD_NUM.getValue());

        int transferLeaderTimeout = electionConfig.getTransferLeaderTimeout();
        Assert.assertEquals(transferLeaderTimeout, ElectionConfigKey.TRANSFER_LEADER_TIMEOUT.getValue());
    }
}
