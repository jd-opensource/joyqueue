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

import org.joyqueue.domain.Producer;
import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

import java.util.Map;

/**
 * 生产者
 * @author lixiaobin6
 * 下午2:41 2018/8/13
 */
public class IgniteProducerConfig implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_APP = "app";
    public static final String COLUMN_NEAR_BY = "near_by";
    public static final String COLUMN_ARCHIVE = "archive";
    public static final String COLUMN_SINGLE = "single";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_BLACK_LIST = "black_list";
    public static final String COLUMN_TIMEOUT = "timeout";
    public static final String COLUMN_LIMIT_TPS = "limit_tps";
    public static final String COLUMN_LIMIT_TRAFFIC = "limit_traffic";

    private IgniteProducer producer;
    private Producer.ProducerPolicy producerPolicy;
    private Producer.ProducerLimitPolicy limitPolicy;

    public IgniteProducerConfig(IgniteProducer producer) {
        this.producer = producer;
        this.producerPolicy = producer.getProducerPolicy();
        this.limitPolicy = producer.getLimitPolicy();
    }
    private String id;
    /**
     * 主键 id:namespace+topic+app
     */

    @Override
    public String getId() {
        if(null==id){
            return producer.getId();
        }
        return id;
    }

    public Producer.ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public Producer.ProducerLimitPolicy getLimitPolicy() {
        return limitPolicy;
    }

    public void setLimitPolicy(Producer.ProducerLimitPolicy limitPolicy) {
        this.limitPolicy = limitPolicy;
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_TOPIC,producer.getTopic().getCode());
        writer.writeString(COLUMN_NAMESPACE, producer.getTopic().getNamespace());
        writer.writeString(COLUMN_APP, producer.getApp());
        if (null != producerPolicy) {
            writer.writeBoolean(COLUMN_NEAR_BY, producerPolicy.getNearby());
            writer.writeBoolean(COLUMN_ARCHIVE, producerPolicy.getArchive());
            writer.writeBoolean(COLUMN_SINGLE, producerPolicy.isSingle());
            if (null != producerPolicy.getBlackList())
                writer.writeString(COLUMN_BLACK_LIST, StringUtils.join(producerPolicy.getBlackList(),","));
            if (null != producerPolicy.getWeight() && producerPolicy.getWeight().size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Short> entry : producerPolicy.getWeight().entrySet()) {
                    stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                }
                writer.writeString(COLUMN_WEIGHT, stringBuilder.substring(0, stringBuilder.length() - 1).toString());
            }
        }
        writer.writeInt(COLUMN_TIMEOUT, producerPolicy.getTimeOut());
        if (limitPolicy != null) {
            writer.writeInt(COLUMN_LIMIT_TPS, limitPolicy.getTps());
            writer.writeInt(COLUMN_LIMIT_TRAFFIC, limitPolicy.getTraffic());
        }
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.id = reader.readString("id");
        this.producerPolicy = Producer.ProducerPolicy.Builder.build()
                .nearby(reader.readBoolean(COLUMN_NEAR_BY))
                .archive(reader.readBoolean(COLUMN_ARCHIVE))
                .single(reader.readBoolean(COLUMN_SINGLE))
                .blackList(reader.readString(COLUMN_BLACK_LIST))
                .weight(reader.readString(COLUMN_WEIGHT)).timeout(reader.readInt(COLUMN_TIMEOUT)).create();
        this.limitPolicy = new Producer.ProducerLimitPolicy(reader.readInt(COLUMN_LIMIT_TPS), reader.readInt(COLUMN_LIMIT_TRAFFIC));
    }
}
