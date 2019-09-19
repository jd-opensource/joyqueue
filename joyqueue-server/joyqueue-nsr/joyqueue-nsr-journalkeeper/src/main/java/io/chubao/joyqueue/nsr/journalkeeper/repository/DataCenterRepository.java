package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.DataCenterDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * DataCenterRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class DataCenterRepository extends BaseRepository {

    private static final String TABLE = "datacenter";
    private static final String COLUMNS = "id, region, code, name, url";
    private static final String UPDATE_COLUMNS = "region = ?, code = ?, name = ?, url = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY code", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?", TABLE);

    public DataCenterRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public DataCenterDTO getById(String id) {
        return queryOnce(DataCenterDTO.class, GET_BY_ID, id);
    }

    public List<DataCenterDTO> getAll() {
        return query(DataCenterDTO.class, GET_ALL);
    }

    public DataCenterDTO add(DataCenterDTO dataCenterDTO) {
        insert(ADD, dataCenterDTO.getId(), dataCenterDTO.getRegion(), dataCenterDTO.getCode(),
                dataCenterDTO.getName(), dataCenterDTO.getUrl());
        return dataCenterDTO;
    }

    public DataCenterDTO update(DataCenterDTO dataCenterDTO) {
        update(UPDATE_BY_ID, dataCenterDTO.getRegion(), dataCenterDTO.getCode(),
                dataCenterDTO.getName(), dataCenterDTO.getUrl(), dataCenterDTO.getId());
        return dataCenterDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}