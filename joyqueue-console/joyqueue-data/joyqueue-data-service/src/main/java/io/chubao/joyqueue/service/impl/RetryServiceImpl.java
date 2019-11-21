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
package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.domain.ConsumeRetry;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.query.QRetry;
import io.chubao.joyqueue.server.retry.api.ConsoleMessageRetry;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;
import io.chubao.joyqueue.server.retry.model.RetryQueryCondition;
import io.chubao.joyqueue.server.retry.model.RetryStatus;
import io.chubao.joyqueue.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.chubao.joyqueue.exception.ServiceException.FORBIDDEN;
import static io.chubao.joyqueue.exception.ServiceException.INTERNAL_SERVER_ERROR;

/**
 * Created by wangxiaofei1 on 2018/12/5.
 */
@Service("retryService")
public class RetryServiceImpl implements RetryService {
    private static  final Logger logger = LoggerFactory.getLogger(RetryServiceImpl.class);

    @Autowired(required = false)
    private ConsoleMessageRetry consoleMessageRetry;

    @Value("${retry.enable:false}")
    private Boolean retryEnable;

    @Override
    public PageResult<ConsumeRetry> findByQuery(QPageQuery<QRetry> qPageQuery) throws JoyQueueException {
        check();
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
    public ConsumeRetry getDataById(Long id,String topic) throws JoyQueueException {
        return consoleMessageRetry.getConsumeRetryById(id,topic);
    }

    @Override
    public void add(RetryMessageModel retryMessageModel) {
        check();
        List<RetryMessageModel> retryMessageModels = new ArrayList<>();
        retryMessageModels.add(retryMessageModel);
        try {
            consoleMessageRetry.addRetry(retryMessageModels);
        } catch (JoyQueueException e) {
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
        check();
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
        check();
        Long[] messageIds = {Long.valueOf(retry.getMessageId())};
        consoleMessageRetry.updateStatus(retry.getTopic(),retry.getApp(),messageIds, RetryStatus.RETRY_ING,retry.getUpdateTime(),retry.getUpdateBy());
    }

    /*
     * @param retry
     * @return
     */
    @Override
    public void batchDelete(RetryQueryCondition retryQueryCondition, Long updateTime, int updateBy) throws Exception {
        consoleMessageRetry.batchUpdateStatus(retryQueryCondition, RetryStatus.RETRY_DELETE,updateTime,updateBy);
    }

    /**
     * 重试服务是否可用
     * @return
     */
    @Override
    public boolean isServerEnabled() {
        return retryEnable != null && retryEnable.booleanValue();
    }

    private void check() {
        if (!isServerEnabled()) {
            throw new ServiceException(FORBIDDEN, "retry service is disabled. please set retry.enable to be true first.");
        }

        if (consoleMessageRetry == null) {
            throw new ServiceException(INTERNAL_SERVER_ERROR, "consoleMessageRetry can not be null. ");
        }
    }
}
