package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.ConfigDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * ConfigRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConfigRepository extends BaseRepository {

    private static final String TABLE = "config";
    private static final String COLUMNS = "id, key, value, `group`";
    private static final String UPDATE_COLUMNS = "key = ?, value = ?, `group` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?", COLUMNS, TABLE);
    private static final String GET_BY_KEY_AND_GROUP = String.format("SELECT %s FROM %s WHERE key = ? AND group = ? ORDER BY key", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY key", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?", TABLE);

    public ConfigRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public ConfigDTO getById(String id) {
        return queryOnce(ConfigDTO.class, GET_BY_ID, id);
    }

    public ConfigDTO getByKeyAndGroup(String key, String group) {
        return queryOnce(ConfigDTO.class, GET_BY_KEY_AND_GROUP, key, group);
    }

    public List<ConfigDTO> getAll() {
        return query(ConfigDTO.class, GET_ALL);
    }

    public ConfigDTO add(ConfigDTO configDTO) {
        insert(ADD, configDTO.getId(), configDTO.getKey(), configDTO.getValue(), configDTO.getGroup());
        return configDTO;
    }

    public ConfigDTO update(ConfigDTO configDTO) {
        update(UPDATE_BY_ID, configDTO.getKey(), configDTO.getValue(), configDTO.getGroup(), configDTO.getId());
        return configDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}