/**
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
package com.jd.journalq.broker.producer;

import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * Created by chengzhiliang on 2018/10/30.
 */
public class ProduceConfig {
    //TODO
    private PropertySupplier propertySupplier;

    public ProduceConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }


    public int getFeedbackTimeout() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.FEEDBACK_TIMEOUT);
    }

    public int getTransactionExpireClearInterval() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_CLEAR_INTERVAL);
    }

    public int getTransactionMaxUncomplete() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_MAX_UNCOMPLETE);
    }

    public int getTransactionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_EXPIRE_TIME);
    }

    public int getFixThreadPoolNThreads() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.FIX_THREAD_POOL_THREADS);
    }

    public int getBrokerQosLevel() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.BROKER_QOS_LEVEL);
    }

    public long getPrintMetricIntervalMs() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRINT_METRIC_INTERVAL_MS);
    }

    public int getBusinessIdLength() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRODUCE_BUSINESSID_lENGTH);
    }
}