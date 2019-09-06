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
    private static final String UPDATE_COLUMNS = "topic = ?, namespace = ?, app = ?, topic_type = ?, client_type = ?, " +
            "referer = ?, consume_policy = ?, retry_policy = ?, limit_policy = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_APP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND app = ? ORDER BY topic",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? ORDER BY topic",
            COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE app = ? OR referer = ? ORDER BY topic",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY topic",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?",
            TABLE);

    public ConsumerRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public ConsumerDTO getById(String id) {
        return queryOnce(ConsumerDTO.class, GET_BY_ID, id);
    }

    public ConsumerDTO getByTopicAndApp(String topic, String namespace, String app) {
        return queryOnce(ConsumerDTO.class, GET_BY_TOPIC_AND_APP, topic, namespace, app);
    }

    public List<ConsumerDTO> getByTopic(String topic, String namespace) {
        return query(ConsumerDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<ConsumerDTO> getByApp(String app) {
        return query(ConsumerDTO.class, GET_BY_APP, app, app);
    }

    public List<ConsumerDTO> getAll() {
        return query(ConsumerDTO.class, GET_ALL);
    }

    public ConsumerDTO add(ConsumerDTO consumerDTO) {
        insert(ADD, consumerDTO.getId(), consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getTopicType(), consumerDTO.getClientType(), consumerDTO.getReferer(), consumerDTO.getConsumePolicy(),
                consumerDTO.getRetryPolicy(), consumerDTO.getLimitPolicy());
        return consumerDTO;
    }

    public ConsumerDTO update(ConsumerDTO consumerDTO) {
        update(UPDATE_BY_ID, consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getTopicType(), consumerDTO.getClientType(), consumerDTO.getReferer(), consumerDTO.getConsumePolicy(),
                consumerDTO.getRetryPolicy(), consumerDTO.getLimitPolicy(), consumerDTO.getId());
        return consumerDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}