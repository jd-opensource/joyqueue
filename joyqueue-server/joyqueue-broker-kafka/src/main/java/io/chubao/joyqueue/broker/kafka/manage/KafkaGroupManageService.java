package io.chubao.joyqueue.broker.kafka.manage;

/**
 * KafkaGroupManageService
 *
 * author: gaohaoxiang
 * date: 2018/11/13
 */
public interface KafkaGroupManageService {

    /**
     * 移除group
     * @param groupId
     * @return
     */
    boolean removeGroup(String groupId);

    /**
     * 重平衡group
     * @param groupId
     * @return
     */
    boolean rebalanceGroup(String groupId);
}