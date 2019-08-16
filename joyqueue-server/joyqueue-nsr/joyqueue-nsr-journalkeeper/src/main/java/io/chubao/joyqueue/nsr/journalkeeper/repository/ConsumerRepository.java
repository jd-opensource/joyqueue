package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.ConsumerDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * ConsumerRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConsumerRepository extends BaseRepository {

    private static final String TABLE = "consumer";
    private static final String COLUMNS = "id, topic, namespace, app, topic_type, client_type, referer, consume_policy, retry_policy, limit_policy";

    private static final String GET_BY_TOPIC_AND_APP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND app = ?", COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ?", COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE app = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?,?,?,?)", TABLE, COLUMNS);

    public ConsumerRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public ConsumerDTO getByTopicAndApp(String topic, String namespace, String app) {
        return queryOnce(ConsumerDTO.class, GET_BY_TOPIC_AND_APP, topic, namespace, app);
    }

    public List<ConsumerDTO> getByTopic(String topic, String namespace) {
        return query(ConsumerDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<ConsumerDTO> getByApp(String app) {
        return query(ConsumerDTO.class, GET_BY_APP, app);
    }

    public ConsumerDTO add(ConsumerDTO consumerDTO) {
        insert(ADD, consumerDTO.getId(), consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getTopicType(), consumerDTO.getClientType(), consumerDTO.getReferer(), consumerDTO.getConsumePolicy(),
                consumerDTO.getRetryPolicy(), consumerDTO.getLimitPolicy());
        return consumerDTO;
    }
}