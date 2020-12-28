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

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/13
 */
public enum ElectionConfigKey implements PropertyDef {
    ELECTION_METADATA("election.metadata.file", "raft_metafile.dat", Type.STRING),
    ELECTION_TIMEOUT("election.election.timeout", 1000 * 5, Type.INT),
    VOTE_TIMEOUT("election.vote.timeout", 1000 * 5, Type.INT),
    EXECUTOR_THREAD_NUM_MIN("election.executor.thread.num.min", 10, Type.INT),
    EXECUTOR_THREAD_NUM_MAX("election.executor.thread.num.max", 50, Type.INT),
    TIMER_SCHEDULE_THREAD_NUM("election.timer.schedule.thread.num", 10, Type.INT),
    HEARTBEAT_TIMEOUT("election.heartbeat.timeout", 1000, Type.INT),
    HEARTBEAT_MAX_TIMEOUT("election.heartbeat.max.timeout", 1000 * 30, Type.INT),
    SEND_COMMAND_TIMEOUT("election.send.command.timeout", 1000 * 5, Type.INT),
    MAX_BATCH_REPLICATE_SIZE("election.max.replicate.length", 1024 * 1024 * 3, Type.INT),
    DISABLE_STORE_TIMEOUT("election.disable.store.timeout", 1000 * 5, Type.INT),
    LISTEN_PORT("election.listen.port", 18001, Type.INT),
    TRANSFER_LEADER_TIMEOUT("election.transfer.leader.timeout", 1000 * 10, Type.INT),
    REPLICATE_CONSUME_POS_INTERVAL("election.replicate.consume.pos.interval", 1000 * 5, Type.INT),
    REPLICATE_THREAD_NUM_MIN("election.replicate.thread.num.min", 10, Type.INT),
    REPLICATE_THREAD_NUM_MAX("election.replicate.thread.num.max", 100, Type.INT),
    COMMAND_QUEUE_SIZE("election.command.queue.size", 1024, Type.INT),
    LOG_INTERVAL("election.log.interval", 3000, Type.INT),
    TRANSFER_LEADER_MIN_LAG("election.transfer.leader.min.lag", 10 * 1024 * 1024L, Type.LONG),
    ENABLE_REBALANCE_LEADER("election.enable.rebalance.leader", false, Type.BOOLEAN),
    MIN_REBALANCE_INTERVAL("election.min.rebalance.interval", 60 * 60 * 1000, Type.INT),
    ENABLE_REPORT_LEADER_PERIODICALLY("election.enable.report.leader.periodically", true, Type.BOOLEAN),
    ENABLE_REPORT_LEADER_PERIODICALLY_FORCE("election.enable.report.leader.periodically.force", false, Type.BOOLEAN),
    ENABLE_ONLINE_NODE_PERIODICALLY("election.enable.onlineNode.periodically", true, Type.BOOLEAN),
    OUTPUT_CONSUME_POS("election.consume.pos.output", false, Type.BOOLEAN),
    CONNECTION_TIMEOUT("election.connection.timeout", 100 * 1, Type.INT),
    CONNECTION_RETRY_DELAY("election.connection.retryDelay", 1000 * 10, Type.INT),
    ENABLE_SHARED_HEARTBEAT("election.enable.shared.heartbeat", false, Type.BOOLEAN),
    ENABLE_REPLICATE_POSITION_V3_PROTOCOL("election.enable.replicate.position.v3.protocol", false, Type.BOOLEAN),

    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

    ElectionConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

}
