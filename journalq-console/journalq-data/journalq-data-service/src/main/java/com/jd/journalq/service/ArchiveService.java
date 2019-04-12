/**
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
package com.jd.journalq.service;

import com.jd.journalq.model.query.QArchive;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.server.archive.store.api.ArchiveStore;
import com.jd.journalq.server.archive.store.model.ConsumeLog;
import com.jd.journalq.server.archive.store.model.SendLog;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/7.
 */
public interface ArchiveService {

    void register(ArchiveStore archiveStore);

    /**
     * 分页查询归档
     * @param qArchive
     * @return
     */
    List<SendLog> findByQuery(QArchive qArchive) throws JMQException;

    /**
     * 按id查询归档
     * @param messageId
     * @return
     */
    SendLog findSendLog(String topic, Long time, String businessId, String messageId) throws JMQException;

    /**
     * 获取消费详情
     * @param messageId
     * @return
     * @throws JMQException
     */
    List<ConsumeLog> findConsumeLog(String messageId, Integer count) throws JMQException;
}
