/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.sql.repository;

import org.joyqueue.nsr.sql.domain.ConfigDTO;

import java.util.List;

/**
 * ConfigRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConfigRepository {

    private static final String TABLE = "`config`";
    private static final String COLUMNS = "`id`, `key`, `value`, `group`";
    private static final String UPDATE_COLUMNS = "`key` = ?, `value` = ?, `group` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ?", COLUMNS, TABLE);
    private static final String GET_BY_KEY_AND_GROUP = String.format("SELECT %s FROM %s WHERE `key` = ? AND `group` = ? ORDER BY `key`", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY `key`", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?", TABLE);

    private BaseRepository baseRepository;

    public ConfigRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public ConfigDTO getById(String id) {
        return baseRepository.queryOnce(ConfigDTO.class, GET_BY_ID, id);
    }

    public ConfigDTO getByKeyAndGroup(String key, String group) {
        return baseRepository.queryOnce(ConfigDTO.class, GET_BY_KEY_AND_GROUP, key, group);
    }

    public List<ConfigDTO> getAll() {
        return baseRepository.query(ConfigDTO.class, GET_ALL);
    }

    public ConfigDTO add(ConfigDTO configDTO) {
        baseRepository.insert(ADD, configDTO.getId(), configDTO.getKey(), configDTO.getValue(), configDTO.getGroup());
        return configDTO;
    }

    public ConfigDTO update(ConfigDTO configDTO) {
        baseRepository.update(UPDATE_BY_ID, configDTO.getKey(), configDTO.getValue(), configDTO.getGroup(), configDTO.getId());
        return configDTO;
    }

    public int deleteById(String id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}