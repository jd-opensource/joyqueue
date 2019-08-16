package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.NamespaceDTO;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * NamespaceRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class NamespaceRepository extends BaseRepository {

    private static final String TABLE = "namespace";
    private static final String COLUMNS = "id, code, name";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE code = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?)", TABLE, COLUMNS);

    public NamespaceRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public NamespaceDTO getById(String id) {
        return queryOnce(NamespaceDTO.class, GET_BY_ID, id);
    }

    public NamespaceDTO getByCode(String code) {
        return queryOnce(NamespaceDTO.class, GET_BY_CODE, code);
    }

    public NamespaceDTO add(NamespaceDTO namespaceDTO) {
        insert(ADD, namespaceDTO.getId(), namespaceDTO.getCode(), namespaceDTO.getName());
        return namespaceDTO;
    }
}