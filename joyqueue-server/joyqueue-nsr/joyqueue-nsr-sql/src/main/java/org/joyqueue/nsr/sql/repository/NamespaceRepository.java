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

import org.joyqueue.nsr.sql.domain.NamespaceDTO;

import java.util.List;

/**
 * NamespaceRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class NamespaceRepository {

    private static final String TABLE = "`namespace`";
    private static final String COLUMNS = "`id`, `code`, `name`";
    private static final String UPDATE_COLUMNS = "`code` = ?, `name` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ? ORDER BY `code`", COLUMNS, TABLE);
    private static final String GET_BY_CODE = String.format("SELECT %s FROM %s WHERE `code` = ?", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY `code`", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?", TABLE);

    private BaseRepository baseRepository;

    public NamespaceRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public NamespaceDTO getById(String id) {
        return baseRepository.queryOnce(NamespaceDTO.class, GET_BY_ID, id);
    }

    public NamespaceDTO getByCode(String code) {
        return baseRepository.queryOnce(NamespaceDTO.class, GET_BY_CODE, code);
    }

    public List<NamespaceDTO> getAll() {
        return baseRepository.query(NamespaceDTO.class, GET_ALL);
    }

    public NamespaceDTO add(NamespaceDTO namespaceDTO) {
        baseRepository.insert(ADD, namespaceDTO.getId(), namespaceDTO.getCode(), namespaceDTO.getName());
        return namespaceDTO;
    }

    public NamespaceDTO update(NamespaceDTO namespaceDTO) {
        baseRepository.update(UPDATE_BY_ID, namespaceDTO.getCode(), namespaceDTO.getName(), namespaceDTO.getId());
        return namespaceDTO;
    }

    public int deleteById(String id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}