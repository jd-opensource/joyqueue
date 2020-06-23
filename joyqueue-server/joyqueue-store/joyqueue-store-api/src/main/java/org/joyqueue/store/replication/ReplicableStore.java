/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.store.replication;

import org.joyqueue.store.ReadException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

/**
 * 选举复制相关的存储接口
 * @author liyue25
 * Date: 2018/8/31
 */
public interface ReplicableStore {
    /**
     * 节点服务状态
     * @return true: 可读写，false：不可读写
     */
    boolean serviceStatus();

    /**
     * 将服务状态置为true
     */
    void enable();

    /**
     * 将服务状态置为false
     */
    void disable();

    /**
     * 设置Store的最大位置。
     * 如果给定 {@code position}：
     * 1. 在最大最小位置之间 {@link #leftPosition()} <= {@code position} < {@link #rightPosition()}：
     *  截断 {@code position}之后的数据。
     * 2. 否则清空存储所有数据，将最大最小位置都置为给定的{@code position}
     * @param position 给定新的最大位置
     * @throws IOException 读写或删除文件异常时抛出
     */
    void setRightPosition(long position) throws IOException;

    /**
     * 当前日志最大位置
     * @return 当前日志最大位置（不含）
     */
    long rightPosition();
    /**
     * 当前日志最小位置
     * @return 当前日志最小位置（含）
     */
    long leftPosition();

    /**
     * 清空Store所有数据，并将所有位置置为position
     * @param position 新的起始位置
     * @throws IOException 发生IO异常时抛出
     */
    void clear(long position) throws IOException;

    /**
     * 当前日志提交的位置
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
     * @throws ReadException {@code position} 不是一条日志的开始位置，或者其他读日志异常。
     * @throws IOException 发生IO错误
     */
    ByteBuffer readEntryBuffer(long position, int length) throws IOException;

    /**
     * 追加写入一段消息数据，给定的ByteBuffer必须满足如下条件：
     * 1. 连续且完整的多条消息。
     * 2. 这些消息具有相同的term。
     * 3. 消息中同一分区内的索引序号必须连续，并且与同分区已有消息的索引序号连续。
     *
     * @param byteBuffer 待写入的ByteBuffer，将position至limit之间的数据写入存储。
     *                   写入完成后， position == limit。
     * @return {@link #rightPosition()}
     * @throws IOException 发生IO错误
     * @throws TimeoutException 等待写入超时，需要重试。
     */
    long appendEntryBuffer(ByteBuffer byteBuffer) throws IOException, TimeoutException;
    
    /**
     * 计算日志相对于position的位置
     * @param position 当前位置，必须是日志的起始位置
     * @param offsetCount 偏移的日志条数，可以为负数
     */
    long position(long position, int offsetCount) throws IOException;

    /**
     * 最后一条消息的Term
     */
    int lastEntryTerm();

    /**
     * LEADER 收到半数以上回复后，调用此方法提交位置。
     * FOLLOWER
     */
    void commit(long position);

    /**
     * 获取指定位置的日志的任期
     * @param position 日志起始位置
     * @throws ReadException {@code position} 不是一条日志的开始位置，或者其他读日志异常。
     * @throws IOException 发生IO错误
     */
    int getEntryTerm(long position) throws IOException;

}
