package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.PartitionGroupReplicaDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * PartitionGroupReplicaRepository
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class PartitionGroupReplicaRepository extends BaseRepository {

    private static final String TABLE = "partition_group_replica";
    private static final String COLUMNS = "id, topic, namespace, broker_id, `group`";
    private static final String UPDATE_COLUMNS = "topic = ?, namespace = ?, broker_id = ?, `group` = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_GROUP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND `group` = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_BROKER = String.format("SELECT %s FROM %s WHERE broker_id = ?",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?",
            TABLE);

    public PartitionGroupReplicaRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public PartitionGroupReplicaDTO getById(String id) {
        return queryOnce(PartitionGroupReplicaDTO.class, GET_BY_ID, id);
    }

    public List<PartitionGroupReplicaDTO> getByTopic(String topic, String namespace) {
        return query(PartitionGroupReplicaDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<PartitionGroupReplicaDTO> getByTopicAndGroup(String topic, String namespace, int group) {
        return query(PartitionGroupReplicaDTO.class, GET_BY_TOPIC_AND_GROUP, topic, namespace, group);
    }

    public List<PartitionGroupReplicaDTO> getByBrokerId(long brokerId) {
        return query(PartitionGroupReplicaDTO.class, GET_BY_BROKER, brokerId);
    }

    public List<PartitionGroupReplicaDTO> getAll() {
        return query(PartitionGroupReplicaDTO.class, GET_ALL);
    }

    public PartitionGroupReplicaDTO add(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        insert(ADD, partitionGroupReplicaDTO.getId(), partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace(),
                partitionGroupReplicaDTO.getBrokerId(), partitionGroupReplicaDTO.getGroup());
        return partitionGroupReplicaDTO;
    }

    public PartitionGroupReplicaDTO update(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        update(UPDATE_BY_ID, partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace(),
                partitionGroupReplicaDTO.getBrokerId(), partitionGroupReplicaDTO.getGroup(), partitionGroupReplicaDTO.getId());
        return partitionGroupReplicaDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}