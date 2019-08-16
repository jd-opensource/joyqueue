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

    private static final String GET_BY_TOPIC_AND_APP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND app = ?", COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ?", COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE app = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?)", TABLE, COLUMNS);

    public ProducerRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
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

    public ProducerDTO add(ProducerDTO consumerDTO) {
        insert(ADD, consumerDTO.getId(), consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getClientType(), consumerDTO.getProducePolicy(), consumerDTO.getLimitPolicy());
        return consumerDTO;
    }
}