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

import org.joyqueue.nsr.sql.domain.DataCenterDTO;

import java.util.List;

/**
 * DataCenterRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class DataCenterRepository {

    private static final String TABLE = "`datacenter`";
    private static final String COLUMNS = "`id`, `region`, `code`, `name`, `url`";
    private static final String UPDATE_COLUMNS = "`region` = ?, `code` = ?, `name` = ?, `url` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ?", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY `code`", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?", TABLE);

    private BaseRepository baseRepository;

    public DataCenterRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public DataCenterDTO getById(String id) {
        return baseRepository.queryOnce(DataCenterDTO.class, GET_BY_ID, id);
    }

    public List<DataCenterDTO> getAll() {
        return baseRepository.query(DataCenterDTO.class, GET_ALL);
    }

    public DataCenterDTO add(DataCenterDTO dataCenterDTO) {
        baseRepository.insert(ADD, dataCenterDTO.getId(), dataCenterDTO.getRegion(), dataCenterDTO.getCode(),
                dataCenterDTO.getName(), dataCenterDTO.getUrl());
        return dataCenterDTO;
    }

    public DataCenterDTO update(DataCenterDTO dataCenterDTO) {
        baseRepository.update(UPDATE_BY_ID, dataCenterDTO.getRegion(), dataCenterDTO.getCode(),
                dataCenterDTO.getName(), dataCenterDTO.getUrl(), dataCenterDTO.getId());
        return dataCenterDTO;
    }

    public int deleteById(String id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}