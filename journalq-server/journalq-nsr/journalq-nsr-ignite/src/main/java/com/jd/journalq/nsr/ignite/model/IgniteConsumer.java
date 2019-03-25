package com.jd.journalq.nsr.ignite.model;

import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.domain.TopicType;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;


/**
 * 订阅
 *
 * @author lixiaobin6
 * 下午3:23 2018/7/31
 */
public class IgniteConsumer extends Consumer implements IgniteBaseModel, Binarylizable {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMESPACE = "namespace";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_APP = "app";
    public static final String COLUMN_CLIENT_TYPE = "client_type";
    public static final String COLUMN_TOPIC_TYPE = "topic_type";
    /**
     * 该字段是app.group中的app
     */
    public static final String COLUMN_REFER = "referer";

    /**
     * id:namespace+topic+app
     */
    private String id;

    public IgniteConsumer() {
    }

    public IgniteConsumer(Consumer consumer) {
        this.app = consumer.getApp();
        this.topic = consumer.getTopic();
        this.clientType = consumer.getClientType();
        this.topicType = consumer.getTopicType();
        this.consumerPolicy = consumer.getConsumerPolicy();
        this.retryPolicy = consumer.getRetryPolicy();
    }

    @Override
    public String getId() {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(app).toString();
    }

    public static String getId(TopicName topic, String app) {
        return new StringBuilder(30).append(topic.getFullName()).append(SPLICE).append(app).toString();
    }

    public IgniteConsumer fillConfig(IgniteConsumerConfig consumerConfig) {
        if (null != consumerConfig) {
            this.consumerPolicy = consumerConfig.getConsumerPolicy();
            this.retryPolicy = consumerConfig.getRetryPolicy();
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
        writer.writeByte(COLUMN_TOPIC_TYPE, topicType.code());
        writer.writeString(COLUMN_REFER, app.split(SEPARATOR_SPLIT)[0]);
    }

    @Override
    public void readBinary(BinaryReader reader) throws BinaryObjectException {
        this.id = reader.readString(COLUMN_ID);
        this.topic = new TopicName(reader.readString(COLUMN_TOPIC), reader.readString(COLUMN_NAMESPACE));
        this.app = reader.readString(COLUMN_APP);
        this.clientType = ClientType.valueOf(reader.readByte(COLUMN_CLIENT_TYPE));
        this.type = Type.CONSUMPTION;
        this.topicType = TopicType.valueOf(reader.readByte(COLUMN_TOPIC_TYPE));
    }
}
