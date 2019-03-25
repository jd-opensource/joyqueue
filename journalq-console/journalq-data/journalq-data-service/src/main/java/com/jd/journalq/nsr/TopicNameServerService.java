package com.jd.journalq.nsr;

import com.jd.journalq.common.domain.PartitionGroup;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QTopic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface TopicNameServerService extends NsrService<Topic,QTopic,String> {
    /**
     * 添加主题
     * @param
     * @param topic
     * @param partitionGroups
     * @throws Exception
     */
    String addTopic(Topic topic, List<TopicPartitionGroup> partitionGroups) throws Exception;
    /**
     * 删除主题
     * @param
     * @param topic
     * @throws Exception
     */
    int removeTopic(Topic topic) throws Exception;

    /**
     * 添加partitionGroup
     * @param partitionGroups
     * @throws Exception
     */
    String addPartitionGroup(TopicPartitionGroup partitionGroups) throws Exception;
    /**
     * 移除partitionGroup
     * @throws Exception
     */
    String removePartitionGroup(TopicPartitionGroup group) throws Exception;
    /**
     * 添加partitionGroup
     * @param partitionGroups
     * @throws Exception
     */
    List<Integer> updatePartitionGroup(TopicPartitionGroup partitionGroups) throws Exception;

    /**
     * leader改变
     * @param group
     */
    int leaderChange(TopicPartitionGroup group);
    /**
     * 查找Master
     * @param replicas
     * @return
     * @throws Exception
     */
    List<PartitionGroup> findPartitionGroupMaster(List<TopicPartitionGroup> replicas) throws Exception;

    /**
     * 查询未订阅的topic
     * @param query
     * @return
     */
    PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query);

    /**
     * 根据code查询
     * @param code
     * @param namespaceCode
     * @return
     */
    Topic findByCode(@Param("namespaceCode") String namespaceCode,@Param("code")String code);

    Topic findById(String id);
}
