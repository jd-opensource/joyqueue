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
package org.joyqueue.nsr.ignite.model;

import org.joyqueue.domain.Consumer;
import org.joyqueue.toolkit.retry.RetryPolicy;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * @author wylixiaobin
 * Date: 2018/9/3
 */
public class IgniteConsumerConfig implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_APP = "app";
    public static final String COLUMN_NEAR_BY = "near_by";
    public static final String COLUMN_ARCHIVE = "archive";
    public static final String COLUMN_PAUSED = "paused";
    public static final String COLUMN_RETRY = "retry";
    public static final String COLUMN_CONCURRENT = "concurrent";
    public static final String COLUMN_ACK_TIMEOUT = "ack_timeout";
    public static final String COLUMN_BATCH_SIZE = "batch_size";
    public static final String COLUMN_DELAY = "delay";
    public static final String COLUMN_FILTERS = "filters";
    public static final String COLUMN_MAX_RETRYS = "max_retrys";
    public static final String COLUMN_MAX_RETRY_DELAY = "max_retry_delay";
    public static final String COLUMN_RETRY_DELAY = "retry_delay";
    public static final String COLUMN_EXPIRE_TIME = "expire_time";
    public static final String COLUMN_BLACK_LIST = "black_list";
    public static final String COLUMN_ERROR_TIMES = "err_times";
    public static final String COLUMN_MAX_PARTITION_NUM = "max_partition_num";
    public static final String COLUMN_RETRY_READ_PROBABILITY = "retry_read_probability";
    public static final String COLUMN_LIMIT_TPS = "limit_tps";
    public static final String COLUMN_LIMIT_TRAFFIC = "limit_traffic";

    private IgniteConsumer consumer;
    /**
     * 消费策略
     */
    private Consumer.ConsumerPolicy consumerPolicy;
    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy;
    /**
     * 限流策略
     */
    private Consumer.ConsumerLimitPolicy limitPolicy;

    private String id;

    public IgniteConsumerConfig(IgniteConsumer consumer) {
        this.consumer = consumer;
        this.consumerPolicy = consumer.getConsumerPolicy();
        this.retryPolicy = consumer.getRetryPolicy();
        this.limitPolicy = consumer.getLimitPolicy();
    }

    public Consumer.ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public void setConsumerPolicy(Consumer.ConsumerPolicy consumerPolicy) {
        this.consumerPolicy = consumerPolicy;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public void setLimitPolicy(Consumer.ConsumerLimitPolicy limitPolicy) {
        this.limitPolicy = limitPolicy;
    }

    public Consumer.ConsumerLimitPolicy getLimitPolicy() {
        return limitPolicy;
    }

    @Override
    public String getId() {
        if (null ==id) {
            return consumer.getId();
        }
        return id;
    }


    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_TOPIC,  consumer.getTopic().getCode());
        writer.writeString(COLUMN_NAMESPACE,  consumer.getTopic().getNamespace());
        writer.writeString(COLUMN_APP, consumer.getApp());
        if (null != consumerPolicy) {
            writer.writeBoolean(COLUMN_NEAR_BY, consumerPolicy.getNearby());
            writer.writeBoolean(COLUMN_ARCHIVE, consumerPolicy.getArchive());
            writer.writeBoolean(COLUMN_RETRY, consumerPolicy.getRetry());
            writer.writeBoolean(COLUMN_PAUSED,consumerPolicy.getPaused());
            writer.writeInt(COLUMN_ACK_TIMEOUT, consumerPolicy.getAckTimeout());
            writer.writeShort(COLUMN_BATCH_SIZE, consumerPolicy.getBatchSize());
            writer.writeInt(COLUMN_CONCURRENT, consumerPolicy.getConcurrent());
            writer.writeInt(COLUMN_ERROR_TIMES,consumerPolicy.getErrTimes());
            writer.writeInt(COLUMN_MAX_PARTITION_NUM,consumerPolicy.getMaxPartitionNum());
            writer.writeInt(COLUMN_RETRY_READ_PROBABILITY,consumerPolicy.getReadRetryProbability());
            writer.writeMap(COLUMN_FILTERS,consumerPolicy.getFilters());
            if (null != consumerPolicy.getBlackList())
                writer.writeString(COLUMN_BLACK_LIST,StringUtils.join(consumerPolicy.getBlackList(),","));
            writer.writeInt(COLUMN_DELAY, consumerPolicy.getDelay());
        }
        if (null != retryPolicy) {
            writer.writeInt(COLUMN_MAX_RETRYS, retryPolicy.getMaxRetrys());
            writer.writeInt(COLUMN_MAX_RETRY_DELAY, retryPolicy.getMaxRetryDelay());
            writer.writeInt(COLUMN_RETRY_DELAY, retryPolicy.getRetryDelay());
            writer.writeInt(COLUMN_EXPIRE_TIME, retryPolicy.getExpireTime());
        }
        if (limitPolicy != null) {
            writer.writeInt(COLUMN_LIMIT_TPS, limitPolicy.getTps());
            writer.writeInt(COLUMN_LIMIT_TRAFFIC, limitPolicy.getTraffic());
        }
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        id = reader.readString(COLUMN_ID);
        this.consumerPolicy = Consumer.ConsumerPolicy.Builder.build()
                .nearby(reader.readBoolean(COLUMN_NEAR_BY))
                .archive(reader.readBoolean(COLUMN_ARCHIVE))
                .retry(reader.readBoolean(COLUMN_RETRY))
                .ackTimeout(reader.readInt(COLUMN_ACK_TIMEOUT))
                .batchSize(reader.readShort(COLUMN_BATCH_SIZE))
                .concurrent(reader.readInt(COLUMN_CONCURRENT))
                .delay(reader.readInt(COLUMN_DELAY))
                .paused(reader.readBoolean(COLUMN_PAUSED))
                .errTimes(reader.readInt(COLUMN_ERROR_TIMES))
                .maxPartitionNum(reader.readInt(COLUMN_MAX_PARTITION_NUM))
                .retryReadProbability(reader.readInt(COLUMN_RETRY_READ_PROBABILITY))
                .blackList(reader.readString(COLUMN_BLACK_LIST))
                .filters(reader.readMap(COLUMN_FILTERS)).create();
        this.retryPolicy = new RetryPolicy.Builder().build()
                .maxRetrys(reader.readInt(COLUMN_MAX_RETRYS))
                .maxRetryDelay(reader.readInt(COLUMN_MAX_RETRY_DELAY))
                .retryDelay(reader.readInt(COLUMN_RETRY_DELAY))
                .expireTime(reader.readInt(COLUMN_EXPIRE_TIME))
                .create();
        this.limitPolicy = new Consumer.ConsumerLimitPolicy(reader.readInt(COLUMN_LIMIT_TPS), reader.readInt(COLUMN_LIMIT_TRAFFIC));
    }
}
