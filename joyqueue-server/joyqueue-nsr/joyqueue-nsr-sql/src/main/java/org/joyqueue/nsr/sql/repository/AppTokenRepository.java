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

import org.joyqueue.nsr.sql.domain.AppTokenDTO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * AppTokenRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class AppTokenRepository {

    private static final String TABLE = "`app_token`";
    private static final String COLUMNS = "`id`, `app`, `token`, `effective_time`, `expiration_time`";
    private static final String UPDATE_COLUMNS = "`app` = ?, `token` = ?, `effective_time` = ?, `expiration_time` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ?", COLUMNS, TABLE);
    private static final String GET_BY_APP_AND_CODE = String.format("SELECT %s FROM %s WHERE `app` = ? AND `token` = ?", COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE `app` = ?", COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)", TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?", TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?", TABLE);

    private BaseRepository baseRepository;

    public AppTokenRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public AppTokenDTO getById(long id) {
        return baseRepository.queryOnce(AppTokenDTO.class, GET_BY_ID, id);
    }

    public AppTokenDTO getByAppAndToken(String app, String token) {
        return baseRepository.queryOnce(AppTokenDTO.class, GET_BY_APP_AND_CODE, app, token);
    }

    public List<AppTokenDTO> getByApp(String app) {
        return baseRepository.query(AppTokenDTO.class, GET_BY_APP, app);
    }

    public List<AppTokenDTO> getAll() {
        return baseRepository.query(AppTokenDTO.class, GET_ALL);
    }

    public AppTokenDTO add(AppTokenDTO appTokenDTO) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        baseRepository.insert(ADD, appTokenDTO.getId(), appTokenDTO.getApp(), appTokenDTO.getToken(),
                format.format(appTokenDTO.getEffectiveTime()), format.format(appTokenDTO.getExpirationTime()));
        return appTokenDTO;
    }

    public AppTokenDTO update(AppTokenDTO appTokenDTO) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        baseRepository.update(UPDATE_BY_ID, appTokenDTO.getApp(), appTokenDTO.getToken(), format.format(appTokenDTO.getEffectiveTime()),
                format.format(appTokenDTO.getExpirationTime()), appTokenDTO.getId());
        return appTokenDTO;
    }

    public int deleteById(long id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}