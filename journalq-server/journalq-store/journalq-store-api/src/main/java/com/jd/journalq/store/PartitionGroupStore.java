package com.jd.journalq.store;

import com.jd.journalq.toolkit.concurrent.EventListener;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * 本地消息数据存储，用于读写消息
 */
public interface PartitionGroupStore {



    String getTopic();
    int getPartitionGroup();
    Short[] listPartitions();

    long getTotalPhysicalStorageSize();

    long deleteMinStoreMessages(long minIndexedPosition) throws IOException;
    long deleteEarlyMinStoreMessages(long targetDeleteTimeline, long minIndexedPosition) throws IOException;

    /**
     * 获取分区当前的最小索引，用于初始化消费
     * @param partition 分区
     * @return 返回值小于0时表示分区不存在，否则返回分区最大索引
     */
    long getLeftIndex(short partition);

    /**
     * 获取分区当前的最大索引，用于初始化消费
     * @param partition 分区
     * @return 返回值小于0时表示分区不存在，否则返回分区最大索引
     */
    long getRightIndex(short partition);

    /**
     * 根据消息存储时间获取索引。
     * 如果找到，返回最后一条 “存储时间 <= timestamp” 消息的索引。
     * 如果找不到，返回负值。
     */
    long getIndex(short partition, long timestamp);

    /**
     * 异步写入消息，线程安全，保证ACI，D的保证取决于WriteQosLevel
     * @param writeRequests partition序号和消息
     * @return 以Future形式返回结果
     * @throws NullPointerException eventListener或writeRequests为空时抛出
     * @see WriteResult
     */
    Future<WriteResult> asyncWrite(WriteRequest... writeRequests);

    /**
     * 异步写入消息，线程安全，保证ACI，D的保证取决于WriteQosLevel
     * @param eventListener 回调方法
     * @param writeRequests partition序号和消息
     * @throws NullPointerException eventListener或writeRequests为空时抛出
     * @see WriteResult
     */
    void asyncWrite(EventListener<WriteResult> eventListener, WriteRequest... writeRequests);



    /**
     * 非阻塞批量读取消息，从指定位置读取消息，如果没有消息立即返回。
     * @param partition partition序号
     * @param index partition内的全局消息序号
     * @param count 要求读取的消息数量，当count < 1 时，按coKunt = 1处理。
     * @param maxSize 返回所有消息的长度之和最大值。参数不大于0时，不限制最大长度。
     *                当第一条消息长度大于maxSize时，返回1条消息。
     *                否则返回尽可能多的消息，保证这些消息长度之不超过maxSize。
     * @return 消息数组
     * @see ReadResult
     * 不保证返回消息数量一定等于要求数量count。
     */
    ReadResult read(short partition, long index, int count, long maxSize) throws IOException;


}
