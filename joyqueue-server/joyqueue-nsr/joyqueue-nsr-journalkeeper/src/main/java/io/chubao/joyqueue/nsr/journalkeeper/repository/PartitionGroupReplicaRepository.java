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

    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ?", COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_GROUP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND group = ?", COLUMNS, TABLE);
    private static final String GET_BY_BROKER = String.format("SELECT %s FROM %s WHERE broker_id = ?", COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?)", TABLE, COLUMNS);

    public PartitionGroupReplicaRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
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

    public PartitionGroupReplicaDTO add(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        insert(ADD, partitionGroupReplicaDTO.getId(), partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace(),
                partitionGroupReplicaDTO.getBrokerId(), partitionGroupReplicaDTO.getGroup());
        return partitionGroupReplicaDTO;
    }
}