package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.TopicDTO;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * TopicRepository
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class TopicRepository extends BaseRepository {

    private static final String TABLE = "topic";
    private static final String COLUMNS = "id, code, namespace, partitions, priority_partitions, type";

    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE code = ? AND namespace = ?", COLUMNS, TABLE);
    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?)", TABLE, COLUMNS);

    public TopicRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public TopicDTO getByCodeAndNamespace(String code, String namespace) {
        return queryOnce(TopicDTO.class, GET_BY_CODE, code, namespace);
    }

    public TopicDTO getById(String id) {
        return queryOnce(TopicDTO.class, GET_BY_ID, id);
    }

    public TopicDTO add(TopicDTO topicDTO) {
        insert(ADD, topicDTO.getId(), topicDTO.getCode(), topicDTO.getNamespace(), topicDTO.getPartitions(),
                topicDTO.getPriorityPartitions(), topicDTO.getType());
        return topicDTO;
    }
}