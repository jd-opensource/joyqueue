package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.NamespaceDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * NamespaceRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class NamespaceRepository extends BaseRepository {

    private static final String TABLE = "namespace";
    private static final String COLUMNS = "id, code, name";
    private static final String UPDATE_COLUMNS = "code = ?, name = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ? ORDER BY code", COLUMNS, TABLE);
    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE code = ?", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY code", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?", TABLE);

    public NamespaceRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public NamespaceDTO getById(String id) {
        return queryOnce(NamespaceDTO.class, GET_BY_ID, id);
    }

    public NamespaceDTO getByCode(String code) {
        return queryOnce(NamespaceDTO.class, GET_BY_CODE, code);
    }

    public List<NamespaceDTO> getAll() {
        return query(NamespaceDTO.class, GET_ALL);
    }

    public NamespaceDTO add(NamespaceDTO namespaceDTO) {
        insert(ADD, namespaceDTO.getId(), namespaceDTO.getCode(), namespaceDTO.getName());
        return namespaceDTO;
    }

    public NamespaceDTO update(NamespaceDTO namespaceDTO) {
        update(UPDATE_BY_ID, namespaceDTO.getCode(), namespaceDTO.getName(), namespaceDTO.getId());
        return namespaceDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}