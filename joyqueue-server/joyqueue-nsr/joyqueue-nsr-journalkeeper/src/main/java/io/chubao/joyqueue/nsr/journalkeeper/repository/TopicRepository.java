package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.TopicDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * TopicRepository
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class TopicRepository extends BaseRepository {

    private static final String TABLE = "topic";
    private static final String COLUMNS = "id, code, namespace, partitions, priority_partitions, type";
    private static final String UPDATE_COLUMNS = "code = ?, namespace = ?, partitions = ?, priority_partitions = ?, type = ?";

    private static final String GET_ALL = String.format("SELECT %s FROM %s", COLUMNS, TABLE);
    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE code = ? AND namespace = ?", COLUMNS, TABLE);
    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?", TABLE);

    public TopicRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public List<TopicDTO> getAll() {
        return query(TopicDTO.class, GET_ALL);
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

    public TopicDTO update(TopicDTO topicDTO) {
        update(UPDATE_BY_ID, topicDTO.getCode(), topicDTO.getNamespace(), topicDTO.getPartitions(),
                topicDTO.getPriorityPartitions(), topicDTO.getType(), topicDTO.getId());
        return topicDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}