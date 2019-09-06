package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.ProducerDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * ConsumerRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ProducerRepository extends BaseRepository {

    private static final String TABLE = "producer";
    private static final String COLUMNS = "id, topic, namespace, app, client_type, produce_policy, limit_policy";
    private static final String UPDATE_COLUMNS = "topic = ?, namespace = ?, app = ?, client_type = ?, produce_policy = ?, limit_policy = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_APP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND app = ? ORDER BY topic",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? ORDER BY topic",
            COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE app = ? ORDER BY topic",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?",
            TABLE);

    public ProducerRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public ProducerDTO getById(String id) {
        return queryOnce(ProducerDTO.class, GET_BY_ID, id);
    }

    public ProducerDTO getByTopicAndApp(String topic, String namespace, String app) {
        return queryOnce(ProducerDTO.class, GET_BY_TOPIC_AND_APP, topic, namespace, app);
    }

    public List<ProducerDTO> getByTopic(String topic, String namespace) {
        return query(ProducerDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<ProducerDTO> getByApp(String app) {
        return query(ProducerDTO.class, GET_BY_APP, app);
    }

    public List<ProducerDTO> getAll() {
        return query(ProducerDTO.class, GET_ALL);
    }

    public ProducerDTO add(ProducerDTO consumerDTO) {
        insert(ADD, consumerDTO.getId(), consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getClientType(), consumerDTO.getProducePolicy(), consumerDTO.getLimitPolicy());
        return consumerDTO;
    }

    public ProducerDTO update(ProducerDTO consumerDTO) {
        update(UPDATE_BY_ID, consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getClientType(), consumerDTO.getProducePolicy(), consumerDTO.getLimitPolicy(), consumerDTO.getId());
        return consumerDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}