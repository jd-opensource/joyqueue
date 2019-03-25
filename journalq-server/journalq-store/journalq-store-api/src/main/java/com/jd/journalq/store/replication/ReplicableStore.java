package com.jd.journalq.store.replication;

import com.jd.journalq.store.PositionOverflowException;
import com.jd.journalq.store.PositionUnderflowException;
import com.jd.journalq.store.ReadException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

/**
 * 非线程安全
 * 选举复制相关的存储接口
 * @author liyue25
 * Date: 2018/8/31
 */
public interface ReplicableStore {
    /**
     * 节点服务状态
     */
    boolean serviceStatus();

    void enable() throws Exception;

    void disable(long timeoutMs) throws IOException;

    void setRightPosition(long position, long timeout) throws IOException, TimeoutException;

    long rightPosition();

    /**
     * 集群提交的位置
     */
    long commitPosition();

    /**
     * 当前任期
     */
    int term();

    /**
     * 修改任期，新任期必须必大于现任期;
     */
    void term(int term);


    /**
     * 读取一段日志数据，起始位置由position指定，最大长度不超过length。
     * 返回的数据是完整的若干条日志。这些日志具有相同的term。
     * @param position 起始位置，必须是一条日志的开始位置。
     * @param length 返回数据的最大长度。
     * @return 返回若干条日志，以ByteBuffer形式存储，Buffer的position为0，limit为返回数据的总长度。
     * @throws PositionUnderflowException position < left
     * @throws PositionOverflowException position >= right
     * @throws ReadException position is not a log-start-position or other read exception.
     * @throws IOException 发生IO错误
     */
    ByteBuffer readEntryBuffer(long position, int length) throws IOException;

    /**
     * 追加写入一段日志数据，给定的ByteBuffer必须满足如下条件：
     * 1. 连续且完整的多条日志。
     * 2. 这些日志具有相同的term。
     * @param byteBuffer 待写入的ByteBuffer，将position至limit之间的数据写入存储。
     *                   写入完成后， position == limit。
     * @return store.rightPosition()
     */
    long appendEntryBuffer(ByteBuffer byteBuffer) throws IOException, TimeoutException, InterruptedException;
    /**
     * 计算日志相对于position的位置
     * @param position 当前位置，必须是日志的起始位置
     * @param offsetCount 偏移的消息条数，可以为负数
     */
    long position(long position, int offsetCount) throws IOException;

    /**
     * LEADER 收到半数以上回复后，调用此方法提交
     * FOLLOWER 收到LEADER 从
     */
    void commit(long position);

    /**
     * 获取指定位置的消息的任期
     * @param position 消息起始位置
     */
    int getEntryTerm(long position) throws IOException;

    /**
     * store current left position
     */
    long leftPosition();
}
