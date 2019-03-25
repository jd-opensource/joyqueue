package com.jd.journalq.broker.kafka.manage;

/**
 * KafkaGroupManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/13
 */
public interface KafkaGroupManageService {

    /**
     * 移除group
     * @param groupId
     * @return
     */
    public boolean removeGroup(String groupId);

    /**
     * 重平衡group
     * @param groupId
     * @return
     */
    public boolean rebalanceGroup(String groupId);
}