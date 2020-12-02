package org.joyqueue.store;

/**
 * RemovedPartitionGroupStore
 * author: gaohaoxiang
 * date: 2020/10/28
 */
public interface RemovedPartitionGroupStore {

    /**
     * 获取Topic
     * @return Topic
     */
    String getTopic();

    /**
     * 获取Partition Group 序号
     * @return Partition Group 序号
     */
    int getPartitionGroup();

    /**
     * 物理删除最左文件
     * @return
     */
    boolean physicalDeleteLeftFile();

    /**
     * 物理删除所有文件，包括目录
     * @return
     */
    boolean physicalDelete();
}