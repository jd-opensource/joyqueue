package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.BrokerDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * BrokerRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class BrokerRepository extends BaseRepository {

    private static final String TABLE = "broker";
    private static final String COLUMNS = "id, ip, port, data_center, retry_type, permission";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_BY_IP_AND_PORT = String.format("SELECT %s FROM %s WHERE ip = ? AND port = ?", COLUMNS, TABLE);
    private static final String GET_BY_RETRY_TYPE = String.format("SELECT %s FROM %s WHERE retry_type = ?", COLUMNS, TABLE);
    private static final String GET_BY_IDS = String.format("SELECT %s FROM %s WHERE id in ", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?)", TABLE, COLUMNS);

    public BrokerRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public BrokerDTO getById(int id) {
        return queryOnce(BrokerDTO.class, GET_BY_ID, id);
    }

    public BrokerDTO getByIpAndPort(String ip, int port) {
        return queryOnce(BrokerDTO.class, GET_BY_IP_AND_PORT, ip, port);
    }

    public List<BrokerDTO> getByRetryType(String retryType) {
        return query(BrokerDTO.class, GET_BY_RETRY_TYPE, retryType);
    }

    public List<BrokerDTO> getByIds(List<Integer> ids) {
        StringBuilder idsSql = new StringBuilder();
        idsSql.append("(");
        for (Integer id : ids) {
            idsSql.append(id);
            idsSql.append(",");
        }
        idsSql.substring(0, idsSql.length());
        idsSql.append(")");

        return query(BrokerDTO.class, GET_BY_IDS + idsSql.toString(), ids);
    }

    public BrokerDTO add(BrokerDTO brokerDTO) {
        insert(ADD, brokerDTO.getId(), brokerDTO.getIp(), brokerDTO.getPort(), brokerDTO.getDataCenter(),
                brokerDTO.getRetryType(), brokerDTO.getPermission());
        return brokerDTO;
    }
}