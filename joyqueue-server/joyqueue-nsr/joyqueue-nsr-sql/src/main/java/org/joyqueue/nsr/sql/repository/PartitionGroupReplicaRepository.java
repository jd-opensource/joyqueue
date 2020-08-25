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

import org.joyqueue.nsr.sql.domain.PartitionGroupReplicaDTO;

import java.util.List;

/**
 * PartitionGroupReplicaRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class PartitionGroupReplicaRepository {

    private static final String TABLE = "`partition_group_replica`";
    private static final String COLUMNS = "`id`, `topic`, `namespace`, `broker_id`, `group`";
    private static final String UPDATE_COLUMNS = "`topic` = ?, `namespace` = ?, `broker_id` = ?, `group` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE `id` = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE `topic` = ? AND `namespace` = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_GROUP = String.format("SELECT %s FROM %s WHERE `topic` = ? AND `namespace` = ? AND `group` = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_BROKER = String.format("SELECT %s FROM %s WHERE `broker_id` = ?",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE `id` = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE `id` = ?",
            TABLE);

    private BaseRepository baseRepository;

    public PartitionGroupReplicaRepository(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public PartitionGroupReplicaDTO getById(String id) {
        return baseRepository.queryOnce(PartitionGroupReplicaDTO.class, GET_BY_ID, id);
    }

    public List<PartitionGroupReplicaDTO> getByTopic(String topic, String namespace) {
        return baseRepository.query(PartitionGroupReplicaDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<PartitionGroupReplicaDTO> getByTopicAndGroup(String topic, String namespace, int group) {
        return baseRepository.query(PartitionGroupReplicaDTO.class, GET_BY_TOPIC_AND_GROUP, topic, namespace, group);
    }

    public List<PartitionGroupReplicaDTO> getByBrokerId(long brokerId) {
        return baseRepository.query(PartitionGroupReplicaDTO.class, GET_BY_BROKER, brokerId);
    }

    public List<PartitionGroupReplicaDTO> getAll() {
        return baseRepository.query(PartitionGroupReplicaDTO.class, GET_ALL);
    }

    public PartitionGroupReplicaDTO add(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        baseRepository.insert(ADD, partitionGroupReplicaDTO.getId(), partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace(),
                partitionGroupReplicaDTO.getBrokerId(), partitionGroupReplicaDTO.getGroup());
        return partitionGroupReplicaDTO;
    }

    public PartitionGroupReplicaDTO update(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        baseRepository.update(UPDATE_BY_ID, partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace(),
                partitionGroupReplicaDTO.getBrokerId(), partitionGroupReplicaDTO.getGroup(), partitionGroupReplicaDTO.getId());
        return partitionGroupReplicaDTO;
    }

    public int deleteById(String id) {
        return baseRepository.delete(DELETE_BY_ID, id);
    }
}