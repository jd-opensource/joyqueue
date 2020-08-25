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

import org.joyqueue.nsr.sql.domain.ProducerDTO;

import java.util.List;

/**
 * ConsumerRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ProducerRepository {

    private static final String TABLE = "`producer`";
    private static final String COLUMNS = "`id`, `topic`, `namespace`, `app`, `client_type`, `produce_policy`, `limit_policy`";
    private static final String UPDATE_COLUMNS = "`topic` = ?, `namespace` = ?, `app` = ?, `client_type` = ?, `produce_policy` = ?, `limit_policy` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_APP = String.format("SELECT %s FROM %s WHERE `topic` = ? AND `namespace` = ? AND `app` = ? ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE `topic` = ? AND `namespace` = ? ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String GET_BY_APP = String.format("SELECT %s FROM %s WHERE `app` = ? ORDER BY `id`",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?",
            TABLE);

    private BaseRepository baseRepository;

    public ProducerRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public ProducerDTO getById(String id) {
        return baseRepository.queryOnce(ProducerDTO.class, GET_BY_ID, id);
    }

    public ProducerDTO getByTopicAndApp(String topic, String namespace, String app) {
        return baseRepository.queryOnce(ProducerDTO.class, GET_BY_TOPIC_AND_APP, topic, namespace, app);
    }

    public List<ProducerDTO> getByTopic(String topic, String namespace) {
        return baseRepository.query(ProducerDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<ProducerDTO> getByApp(String app) {
        return baseRepository.query(ProducerDTO.class, GET_BY_APP, app);
    }

    public List<ProducerDTO> getAll() {
        return baseRepository.query(ProducerDTO.class, GET_ALL);
    }

    public ProducerDTO add(ProducerDTO consumerDTO) {
        baseRepository.insert(ADD, consumerDTO.getId(), consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getClientType(), consumerDTO.getProducePolicy(), consumerDTO.getLimitPolicy());
        return consumerDTO;
    }

    public ProducerDTO update(ProducerDTO consumerDTO) {
        baseRepository.update(UPDATE_BY_ID, consumerDTO.getTopic(), consumerDTO.getNamespace(), consumerDTO.getApp(),
                consumerDTO.getClientType(), consumerDTO.getProducePolicy(), consumerDTO.getLimitPolicy(), consumerDTO.getId());
        return consumerDTO;
    }

    public int deleteById(String id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}