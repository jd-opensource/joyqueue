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
package com.jd.journalq.service.impl;

import com.jd.journalq.domain.ConsumeRetry;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.query.QRetry;
import com.jd.journalq.server.retry.api.ConsoleMessageRetry;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.server.retry.model.RetryQueryCondition;
import com.jd.journalq.server.retry.model.RetryStatus;
import com.jd.journalq.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2018/12/5.
 */
@Service("retryService")
public class RetryServiceImpl implements RetryService {
    private static  final Logger logger = LoggerFactory.getLogger(RetryServiceImpl.class);

    @Autowired(required = false)
    private ConsoleMessageRetry consoleMessageRetry;


    @Override
    public PageResult<ConsumeRetry> findByQuery(QPageQuery<QRetry> qPageQuery) throws JournalqException {
        RetryQueryCondition queryCondition = new RetryQueryCondition();
        if (qPageQuery != null) {
            QRetry qRetry = qPageQuery.getQuery();
            queryCondition.setPagination(qPageQuery.getPagination());
            if (qRetry != null) {
                queryCondition.setApp(qRetry.getApp());
                queryCondition.setTopic(qRetry.getTopic());
                queryCondition.setBusinessId(qRetry.getBusinessId());
                if (qRetry.getBeginTime() != null) {
                    queryCondition.setStartTime(qRetry.getBeginTime().getTime());
                }
                if (qRetry.getEndTime() != null) {
                    queryCondition.setEndTime(qRetry.getEndTime().getTime());
                }
                if (qRetry.getStatus() != null) {
                    queryCondition.setStatus(qRetry.getStatus().shortValue());
                }
            }
        }
        PageResult<ConsumeRetry> consumeRetryPageResult = consoleMessageRetry.queryConsumeRetryList(queryCondition);
        if (consumeRetryPageResult.getResult() == null) return PageResult.empty();
        return consumeRetryPageResult;
    }

    @Override
    public ConsumeRetry getDataById(Long id) throws JournalqException {
        return consoleMessageRetry.getConsumeRetryById(id);
    }

    @Override
    public void add(RetryMessageModel retryMessageModel) {
        List<RetryMessageModel> retryMessageModels = new ArrayList<>();
        retryMessageModels.add(retryMessageModel);
        try {
            consoleMessageRetry.addRetry(retryMessageModels);
        } catch (JournalqException e) {
            throw new RuntimeException("add retry error",e);
        }
    }
    /**
     * 恢复不修改缓存
     * broker定时去db拉取要重试的消息
     * 最少 30s，最多5分钟
     * @param retry
     * @return
     */
    @Override
    public void recover(ConsumeRetry retry) throws Exception {
        Long[] messageIds = {Long.valueOf(retry.getMessageId())};
        consoleMessageRetry.updateStatus(retry.getTopic(),retry.getApp(),messageIds, RetryStatus.RETRY_ING,retry.getUpdateTime(),retry.getUpdateBy());
    }

    /**
     * 删除缓存
     * @param retry
     * @return
     */
    @Override
    public void delete(ConsumeRetry retry) throws Exception {
        Long[] messageIds = {Long.valueOf(retry.getMessageId())};
        consoleMessageRetry.updateStatus(retry.getTopic(),retry.getApp(),messageIds, RetryStatus.RETRY_ING,retry.getUpdateTime(),retry.getUpdateBy());
    }
}
