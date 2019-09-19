package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.domain.PartitionGroupDTO;
import io.journalkeeper.sql.client.SQLOperator;

import java.util.List;

/**
 * PartitionGroupRepository
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class PartitionGroupRepository extends BaseRepository {

    private static final String TABLE = "partition_group";
    private static final String COLUMNS = "id, namespace, topic, `group`, leader, isrs, term, partitions, learners, " +
            "replicas, out_sync_replicas, elect_type, rec_leader";
    private static final String UPDATE_COLUMNS = "namespace = ?, topic = ?, `group` = ?, leader = ?, isrs = ?, term = ?, partitions = ?, learners = ?, " +
            "replicas = ?, out_sync_replicas = ?, elect_type = ?, rec_leader = ?";

    private static final String GET_BY_ID = String.format("SELECT %s FROM %s WHERE id = ?",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC_AND_GROUP = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? AND `group` = ? ORDER BY `group`",
            COLUMNS, TABLE);
    private static final String GET_BY_TOPIC = String.format("SELECT %s FROM %s WHERE topic = ? AND namespace = ? ORDER BY `group`",
            COLUMNS, TABLE);
    private static final String GET_ALL = String.format("SELECT %s FROM %s",
            COLUMNS, TABLE);
    private static final String ADD = String.format("INSERT INTO %s(%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
            TABLE, COLUMNS);
    private static final String UPDATE_BY_ID = String.format("UPDATE %s SET %s WHERE id = ?",
            TABLE, UPDATE_COLUMNS);
    private static final String UPDATE_LEADER_BY_ID = String.format("UPDATE %s SET leader = ?, term = ?, isrs = ? WHERE id = ?",
            TABLE);
    private static final String DELETE_BY_ID = String.format("DELETE FROM %s WHERE id = ?",
            TABLE);

    public PartitionGroupRepository(SQLOperator sqlOperator) {
        super(sqlOperator);
    }

    public PartitionGroupDTO getById(String id) {
        return queryOnce(PartitionGroupDTO.class, GET_BY_ID, id);
    }

    public PartitionGroupDTO getByTopicAndGroup(String topic, String namespace, int group) {
        return queryOnce(PartitionGroupDTO.class, GET_BY_TOPIC_AND_GROUP, topic, namespace, group);
    }

    public List<PartitionGroupDTO> getByTopic(String topic, String namespace) {
        return query(PartitionGroupDTO.class, GET_BY_TOPIC, topic, namespace);
    }

    public List<PartitionGroupDTO> getAll() {
        return query(PartitionGroupDTO.class, GET_ALL);
    }

    public PartitionGroupDTO add(PartitionGroupDTO partitionGroupDTO) {
        insert(ADD, partitionGroupDTO.getId(), partitionGroupDTO.getNamespace(), partitionGroupDTO.getTopic(),
                partitionGroupDTO.getGroup(), partitionGroupDTO.getLeader(), partitionGroupDTO.getIsrs(),
                partitionGroupDTO.getTerm(), partitionGroupDTO.getPartitions(), partitionGroupDTO.getLearners(),
                partitionGroupDTO.getReplicas(), partitionGroupDTO.getOutSyncReplicas(), partitionGroupDTO.getElectType(), partitionGroupDTO.getRecLeader());
        return partitionGroupDTO;
    }

    public PartitionGroupDTO updateLeader(PartitionGroupDTO partitionGroupDTO) {
        update(UPDATE_LEADER_BY_ID, partitionGroupDTO.getLeader(), partitionGroupDTO.getTerm(), partitionGroupDTO.getIsrs(),
                partitionGroupDTO.getId());
        return partitionGroupDTO;
    }

    public PartitionGroupDTO update(PartitionGroupDTO partitionGroupDTO) {
        update(UPDATE_BY_ID, partitionGroupDTO.getNamespace(), partitionGroupDTO.getTopic(), partitionGroupDTO.getGroup(),
                partitionGroupDTO.getLeader(), partitionGroupDTO.getIsrs(), partitionGroupDTO.getTerm(), partitionGroupDTO.getPartitions(),
                partitionGroupDTO.getLearners(), partitionGroupDTO.getReplicas(), partitionGroupDTO.getOutSyncReplicas(),
                partitionGroupDTO.getElectType(), partitionGroupDTO.getRecLeader(), partitionGroupDTO.getId());
        return partitionGroupDTO;
    }

    public int deleteById(String id) {
        return delete(DELETE_BY_ID, id);
    }
}