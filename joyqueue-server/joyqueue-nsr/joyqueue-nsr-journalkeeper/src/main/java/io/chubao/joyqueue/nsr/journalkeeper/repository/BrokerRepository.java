package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.domain.BrokerDTO;
import io.chubao.joyqueue.nsr.model.BrokerQuery;
import io.journalkeeper.sql.client.SQLOperator;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * BrokerRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class BrokerRepository extends BaseRepository {

    private static final String TABLE = "broker";
    private static final String COLUMNS = "id, ip, port, data_center, retry_type, permission";
    private static final String UPDATE_COLUMNS = "ip = ?, port = ?, data_center = ?, retry_type = ?, permission = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_BY_IP_AND_PORT = String.format("SELECT %s FROM %s WHERE ip = ? AND port = ? ORDER BY ip", COLUMNS, TABLE);
    private static final String GET_BY_RETRY_TYPE = String.format("SELECT %s FROM %s WHERE retry_type = ? ORDER BY ip", COLUMNS, TABLE);
    private static final String GET_BY_IDS = String.format("SELECT %s FROM %s WHERE id in ", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY ip", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?", TABLE);

    public BrokerRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public BrokerDTO getById(long id) {
        return queryOnce(BrokerDTO.class, GET_BY_ID, id);
    }

    public BrokerDTO getByIpAndPort(String ip, int port) {
        return queryOnce(BrokerDTO.class, GET_BY_IP_AND_PORT, ip, port);
    }

    public List<BrokerDTO> getByRetryType(String retryType) {
        return query(BrokerDTO.class, GET_BY_RETRY_TYPE, retryType);
    }

    public List<BrokerDTO> getByIds(List ids) {
        StringBuilder idsSql = new StringBuilder();
        idsSql.append("(");
        for (int i = 0; i < ids.size(); i++) {
            idsSql.append("?");
            if (i != ids.size() - 1) {
                idsSql.append(",");
            }
        }
        idsSql.append(")");
        return query(BrokerDTO.class, GET_BY_IDS + idsSql.toString(), ids);
    }

    public List<BrokerDTO> getAll() {
        return query(BrokerDTO.class, GET_ALL);
    }

    public int getSearchCount(BrokerQuery query) {
        List<Object> params = new LinkedList<>();
        String sql = getSearchSql(String.format("SELECT COUNT(*) FROM %s WHERE 1 = 1", TABLE), query, params);

        return count(sql, params.toArray(new Object[]{}));
    }

    public List<BrokerDTO> search(QPageQuery<BrokerQuery> pageQuery) {
        Pagination pagination = pageQuery.getPagination();
        List<Object> params = new LinkedList<>();
        StringBuilder sql = new StringBuilder(getSearchSql(String.format("SELECT %s FROM %s WHERE 1 = 1", COLUMNS, TABLE), pageQuery.getQuery(), params));

        if (pagination != null) {
            sql.append(String.format(" LIMIT %s, %s", ((pagination.getPage() - 1) * pagination.getSize()), pagination.getSize()));
        }

        return query(BrokerDTO.class, sql.toString(), params.toArray(new Object[]{}));
    }

    protected String getSearchSql(String prefix, BrokerQuery query, List<Object> params) {
        StringBuilder sql = new StringBuilder(prefix);

        if (StringUtils.isNotBlank(query.getIp())) {
            sql.append(" AND ip = ?");
            params.add(query.getIp());
        }

        if (query.getPort() > 0) {
            sql.append(" AND port = ?");
            params.add(query.getPort());
        }

        if (StringUtils.isNotBlank(query.getRetryType())) {
            sql.append(" AND retry_type = ?");
            params.add(query.getRetryType());
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            if (StringUtils.isNumeric(query.getKeyword())) {
                sql.append(" AND id = ?");
                params.add(query.getKeyword());
            } else {
                sql.append(" AND ip LIKE");
                params.add(query.getKeyword() + "%s");
            }
        }
        return sql.toString();
    }

    public BrokerDTO add(BrokerDTO brokerDTO) {
        insert(ADD, brokerDTO.getId(), brokerDTO.getIp(), brokerDTO.getPort(), brokerDTO.getDataCenter(),
                brokerDTO.getRetryType(), brokerDTO.getPermission());
        return brokerDTO;
    }

    public BrokerDTO update(BrokerDTO brokerDTO) {
        update(UPDATE_BY_ID, brokerDTO.getIp(), brokerDTO.getPort(), brokerDTO.getDataCenter(),
                brokerDTO.getRetryType(), brokerDTO.getPermission(), brokerDTO.getId());
        return brokerDTO;
    }

    public int deleteById(long id) {
        return delete(DELETE_BY_ID, id);
    }
}