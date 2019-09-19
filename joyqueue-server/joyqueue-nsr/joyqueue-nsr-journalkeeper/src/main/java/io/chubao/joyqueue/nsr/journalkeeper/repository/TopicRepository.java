package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.domain.TopicDTO;
import io.chubao.joyqueue.nsr.model.TopicQuery;
import io.journalkeeper.sql.client.SQLOperator;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
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

    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY code", COLUMNS, TABLE);
    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE code = ? AND namespace = ?", COLUMNS, TABLE);
    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?", TABLE, UPDATE_COLUMNS);
    private static final String UPDATE_INCR_PARTITION_BY_ID = String.format("UPDATE %s SET partitions = partitions + ? WHERE id = ?", TABLE);
    private static final String UPDATE_DECR_PARTITION_BY_ID = String.format("UPDATE %s SET partitions = partitions - ? WHERE id = ?", TABLE);
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

    public int getSearchCount(TopicQuery query) {
        List<Object> params = new LinkedList<>();
        String sql = getSearchSql(String.format("SELECT COUNT(*) FROM %s WHERE 1 = 1", TABLE), query, params);

        return count(sql, params.toArray(new Object[]{}));
    }

    public List<TopicDTO> search(QPageQuery<TopicQuery> pageQuery) {
        Pagination pagination = pageQuery.getPagination();
        List<Object> params = new LinkedList<>();
        StringBuilder sql = new StringBuilder(getSearchSql(String.format("SELECT %s FROM %s WHERE 1 = 1", COLUMNS, TABLE), pageQuery.getQuery(), params));

        if (pagination != null) {
            sql.append(String.format(" LIMIT %s, %s", ((pagination.getPage() - 1) * pagination.getSize()), pagination.getSize()));
        }

        return query(TopicDTO.class, sql.toString(), params.toArray(new Object[]{}));
    }

    protected String getSearchSql(String prefix, TopicQuery query, List<Object> params) {
        StringBuilder sql = new StringBuilder(prefix);

        if (StringUtils.isNotBlank(query.getCode())) {
            sql.append(" AND code = ?");
            params.add(query.getCode());
        }

        if (StringUtils.isNotBlank(query.getNamespace())) {
            sql.append(" AND namespace = ?");
            params.add(query.getNamespace());
        }

        if (query.getType() != null) {
            sql.append(" AND type = ?");
            params.add(query.getType());
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            sql.append(" AND code LIKE ?");
            params.add(query.getKeyword() + "%");
        }
        return sql.toString();
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

    public int incrPartitions(String id, int value) {
        return update(UPDATE_INCR_PARTITION_BY_ID, value, id);
    }

    public int decrPartitions(String id, int value) {
        return update(UPDATE_DECR_PARTITION_BY_ID, value, id);
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}