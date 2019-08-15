package io.chubao.joyqueue.broker.manage.service;

public interface ElectionManageService {

    /**
     * 恢复选举元数据
     */
    void restoreElectionMetadata();

    /**
     * 返回当前选举元数据
     *
     * @return 元数据
     */
    String describe();

    /**
     * 返回主题下分区组的选举元数据
     *
     * @param topic 主题
     * @param partitionGroup 分区组组
     * @return 元数据
     */
    String describeTopic(String topic, int partitionGroup);

    /**
     * 更新主题下分区组的选举轮次
     * @param topic 主题
     * @param partitionGroup 分区组
     * @param term 轮次
     */
    void updateTerm(String topic, int partitionGroup, int term);
}
