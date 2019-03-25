package com.jd.journalq.nsr.ignite.model;

import com.jd.journalq.common.domain.ClientType;
import com.jd.journalq.common.domain.Producer;
import com.jd.journalq.common.domain.TopicName;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;

/**
 * 生产者
 * @author lixiaobin6
 * 下午2:41 2018/8/13
 */
public class IgniteProducer extends Producer implements IgniteBaseModel, Binarylizable {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_APP = "app";
    public static final String COLUMN_CLIENT_TYPE = "client_type";

    //protected String id;

    @Override
    public String getId() {
        return new StringBuilder(topic.getFullName()).append(SPLICE).append(app).toString();
    }

    public static String getId(TopicName topic, String app) {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(app).toString();
    }

    public IgniteProducer(Producer producer) {
        this.app = producer.getApp();
        this.clientType = producer.getClientType();
        this.topic = producer.getTopic();
        this.producerPolicy = producer.getProducerPolicy();
    }

    public IgniteProducer fillConfig(IgniteProducerConfig producerConfig) {
        if (null != producerConfig) {
            this.producerPolicy = producerConfig.getProducerPolicy();
        }
        return this;
    }

    @Override
    public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeString(COLUMN_ID, getId());
        writer.writeString(COLUMN_TOPIC, topic.getCode());
        writer.writeString(COLUMN_NAMESPACE, topic.getNamespace());
        writer.writeString(COLUMN_APP, app);
        writer.writeByte(COLUMN_CLIENT_TYPE, clientType.value());
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        //this.id = reader.readString(COLUMN_ID);
        this.topic = new TopicName(reader.readString(COLUMN_TOPIC), reader.readString(COLUMN_NAMESPACE));
        this.app = reader.readString(COLUMN_APP);
        this.clientType = ClientType.valueOf(reader.readByte(COLUMN_CLIENT_TYPE));
        this.type=Type.PRODUCTION;
    }
}
