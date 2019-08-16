package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.DataCenterDTO;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * DataCenterRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class DataCenterRepository extends BaseRepository {

    private static final String TABLE = "datacenter";
    private static final String COLUMNS = "id, region, code, name, url";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE code = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)", TABLE, COLUMNS);

    public DataCenterRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public DataCenterDTO getById(String id) {
        return queryOnce(DataCenterDTO.class, GET_BY_ID, id);
    }

    public DataCenterDTO getByCode(String code) {
        return queryOnce(DataCenterDTO.class, GET_BY_CODE, code);
    }

    public DataCenterDTO add(DataCenterDTO dataCenterDTO) {
        insert(ADD, dataCenterDTO.getId(), dataCenterDTO.getRegion(), dataCenterDTO.getCode(),
                dataCenterDTO.getName(), dataCenterDTO.getUrl());
        return dataCenterDTO;
    }
}