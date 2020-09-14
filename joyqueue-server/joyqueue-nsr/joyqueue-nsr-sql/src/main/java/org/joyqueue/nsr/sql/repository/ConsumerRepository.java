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

import org.joyqueue.nsr.sql.domain.ConsumerDTO;

import java.util.List;

/**
 * ConsumerRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConsumerRepository {

    private static final String TABLE = "`consumer`";
    private static final String COLUMNS = "`id`, `topic`, `namespace`, `app`, `topic_type`, `client_type`, `referer`, `group`, `consume_policy`, `retry_policy`, `limit_policy`";
    private static final String UPDATE_COLUMNS = "`topic` = ?, `namespace` = ?, `app` = ?, `topic_type` = ?, `client_type` = ?, " +
            "`referer` = ?, `consume_policy` = ?, `retry_policy` = ?, `limit_policy` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_APP = String.format("SELECT %s FROM %s WHERE `topic` = ? AND `namespace` = ? AND `app` = ? ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE `topic` = ? AND `namespace` = ? ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE `app` = ? OR `referer` = ? ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?",
            TABLE);

    private BaseRepository baseRepository;

    public ConsumerRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public ConsumerDTO getById(String id) {
        return baseRepository.queryOnce(ConsumerDTO.class, GET_BY_ID, id);
    }

    public ConsumerDTO getByTopicAndApp(String topic, String namespace, String app) {
        return baseRepository.queryOnce(ConsumerDTO.class, GET_BY_TOPIC_AND_APP, topic, namespace, app);
    }

    public List<ConsumerDTO> getByTopic(String topic, String namespace) {
        return baseRepository.query(ConsumerDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<ConsumerDTO> getByApp(String app) {
        return baseRepository.query(ConsumerDTO.class, GET_BY_APP, app, app);
    }

    public List<ConsumerDTO> getAll() {
        return baseRepository.query(ConsumerDTO.class, GET_ALL);
    }

    public ConsumerDTO add(ConsumerDTO consumerDTO) {
        baseRepository.insert(ADD, consumerDTO.getId(), consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getTopicType(), consumerDTO.getClientType(), consumerDTO.getReferer(), consumerDTO.getGroup(),
                consumerDTO.getConsumePolicy(), consumerDTO.getRetryPolicy(), consumerDTO.getLimitPolicy());
        return consumerDTO;
    }

    public ConsumerDTO update(ConsumerDTO consumerDTO) {
        baseRepository.update(UPDATE_BY_ID, consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getTopicType(), consumerDTO.getClientType(), consumerDTO.getReferer(), consumerDTO.getConsumePolicy(),
                consumerDTO.getRetryPolicy(), consumerDTO.getLimitPolicy(), consumerDTO.getId());
        return consumerDTO;
    }

    public int deleteById(String id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}