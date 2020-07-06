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
package org.joyqueue.server.archive.store.api;

import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.server.archive.store.model.AchivePosition;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.server.archive.store.model.Query;
import org.joyqueue.server.archive.store.model.SendLog;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * 归档接口
 * <br>
 * 持久化消费信息、生产信息、归档进度信息
 * <p>
 * Created by chengzhiliang on 2018/12/4.
 */
public interface ArchiveStore extends LifeCycle {

    /**
     * 持久化消费日志
     *
     * @param consumeLogs
     * @throws JoyQueueException
     */
    void putConsumeLog(List<ConsumeLog> consumeLogs, PointTracer tracer) throws JoyQueueException;

    /**
     * 持久化发送日志
     *
     * @param sendLogs
     * @throws JoyQueueException
     */
    void putSendLog(List<SendLog> sendLogs, PointTracer tracer) throws JoyQueueException;

    /**
     * 持久化归档进度信息
     *
     * @param achivePosition
     * @throws JoyQueueException
     */
    void putPosition(AchivePosition achivePosition) throws JoyQueueException;

    /**
     * 查询归档进度信息
     *
     * @param topic
     * @param partition
     * @return
     * @throws JoyQueueException
     */
    Long getPosition(String topic, short partition) throws JoyQueueException;

    /**
     *  Clean topic partition archive position when archive service off
     *
     **/
    void cleanPosition(String topic,short partition) throws JoyQueueException;

    /**
     * 查看发送日志
     *
     * @param query
     * @return
     * @throws JoyQueueException
     */
    List<SendLog> scanSendLog(Query query) throws JoyQueueException;

    /**
     * 查看一条发送日志
     *
     * @param query
     * @return
     * @throws JoyQueueException
     */
    SendLog getOneSendLog(Query query) throws JoyQueueException;

    /**
     * 查询消费日志
     *
     * @param messageId
     * @param count
     * @return
     * @throws JoyQueueException
     */
    List<ConsumeLog> scanConsumeLog(String messageId,Integer count) throws JoyQueueException;

    /**
     * 设置归档存储的命名空间
     *
     * @param nameSpace
     */
    void setNameSpace(String nameSpace);
}
