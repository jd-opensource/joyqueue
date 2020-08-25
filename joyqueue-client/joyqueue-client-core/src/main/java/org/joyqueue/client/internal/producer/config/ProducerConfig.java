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
package org.joyqueue.client.internal.producer.config;

import org.joyqueue.client.internal.common.compress.support.ZlibCompressor;
import org.joyqueue.client.internal.producer.support.WeightedPartitionSelector;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.toolkit.retry.RetryPolicy;

/**
 * ProducerConfig
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class ProducerConfig {

    public static final long NONE_PRODUCE_TIMEOUT = -1;

    private String app;
    private long timeout = 1000 * 3;
    private long produceTimeout = 1000 * 3;
    private long transactionTimeout = 1000 * 60 * 30;

    private boolean failover = true;
    private RetryPolicy retryPolicy = new RetryPolicy(1000 * 1, 2);
    private QosLevel qosLevel = QosLevel.REPLICATION;

    private boolean compress = true;
    private String compressType = ZlibCompressor.NAME;
    private int compressThreshold = 100;

    private boolean batch = true;

    private String selectorType = WeightedPartitionSelector.NAME;
    private int businessIdLengthLimit = 100;
    private int bodyLengthLimit = 1024 * 1024 * 1;
    private int batchBodyLengthLimit = 1024 * 1024 * 4;

    public ProducerConfig copy() {
        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setApp(app);
        producerConfig.setTimeout(timeout);
        producerConfig.setProduceTimeout(produceTimeout);
        producerConfig.setTransactionTimeout(transactionTimeout);
        producerConfig.setFailover(failover);
        producerConfig.setRetryPolicy(retryPolicy);
        producerConfig.setQosLevel(qosLevel);
        producerConfig.setCompress(compress);
        producerConfig.setCompressType(compressType);
        producerConfig.setCompressThreshold(compressThreshold);
        producerConfig.setBatch(batch);
        producerConfig.setSelectorType(selectorType);
        producerConfig.setBusinessIdLengthLimit(businessIdLengthLimit);
        producerConfig.setBodyLengthLimit(bodyLengthLimit);
        producerConfig.setBatchBodyLengthLimit(batchBodyLengthLimit);
        return producerConfig;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getProduceTimeout() {
        return produceTimeout;
    }

    public void setProduceTimeout(long produceTimeout) {
        this.produceTimeout = produceTimeout;
    }

    public long getTransactionTimeout() {
        return transactionTimeout;
    }

    public void setTransactionTimeout(long transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public QosLevel getQosLevel() {
        return qosLevel;
    }

    public void setQosLevel(QosLevel qosLevel) {
        this.qosLevel = qosLevel;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressThreshold(int compressThreshold) {
        this.compressThreshold = compressThreshold;
    }

    public int getCompressThreshold() {
        return compressThreshold;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public boolean isBatch() {
        return batch;
    }

    public String getSelectorType() {
        return selectorType;
    }

    public void setSelectorType(String selectorType) {
        this.selectorType = selectorType;
    }

    public int getBusinessIdLengthLimit() {
        return businessIdLengthLimit;
    }

    public void setBusinessIdLengthLimit(int businessIdLengthLimit) {
        this.businessIdLengthLimit = businessIdLengthLimit;
    }

    public int getBodyLengthLimit() {
        return bodyLengthLimit;
    }

    public void setBodyLengthLimit(int bodyLengthLimit) {
        this.bodyLengthLimit = bodyLengthLimit;
    }

    public int getBatchBodyLengthLimit() {
        return batchBodyLengthLimit;
    }

    public void setBatchBodyLengthLimit(int batchBodyLengthLimit) {
        this.batchBodyLengthLimit = batchBodyLengthLimit;
    }
}