package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.ConfigDTO;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * ConfigRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConfigRepository extends BaseRepository {

    private static final String TABLE = "config";
    private static final String COLUMNS = "id, key, value, `group`";

    private static final String GET_BY_KEY_AND_GROUP = String.format("SELECT %s FROM %s WHERE key = ? AND group = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?)", TABLE, COLUMNS);

    public ConfigRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public ConfigDTO getByKeyAndGroup(String key, String group) {
        return queryOnce(ConfigDTO.class, GET_BY_KEY_AND_GROUP, key, group);
    }

    public ConfigDTO add(ConfigDTO configDTO) {
        insert(ADD, configDTO.getId(), configDTO.getKey(), configDTO.getValue(), configDTO.getGroup());
        return configDTO;
    }
}