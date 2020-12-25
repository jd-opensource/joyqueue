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
package org.joyqueue.broker.archive;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.limit.SubscribeRateLimiter;
import org.joyqueue.toolkit.lang.Close;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Created by chengzhiliang on 2018/12/6.
 */
public class ArchiveManager extends Service {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveManager.class);

    // 集群管理
    private BrokerContext context;
    // 发送归档服务
    private ProduceArchiveService sendArchiveService;
    // 消费归档服务
    private ConsumeArchiveService consumeArchiveService;

    // 归档配置
    private ArchiveConfig archiveConfig;

    // 归档限流器
    private SubscribeRateLimiter rateLimiterManager;

    public ArchiveManager(BrokerContext context) {
        this.context = context;
    }

    public ArchiveManager(ProduceArchiveService sendArchiveService, ConsumeArchiveService consumeArchiveService) {
        this.sendArchiveService = sendArchiveService;
        this.consumeArchiveService = consumeArchiveService;
    }


    public ProduceArchiveService getSendArchiveService() {
        return sendArchiveService;
    }

    public void setSendArchiveService(ProduceArchiveService sendArchiveService) {
        this.sendArchiveService = sendArchiveService;
    }

    public ConsumeArchiveService getConsumeArchiveService() {
        return consumeArchiveService;
    }

    public void setConsumeArchiveService(ConsumeArchiveService consumeArchiveService) {
        this.consumeArchiveService = consumeArchiveService;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (archiveConfig == null) {
            archiveConfig = new ArchiveConfig(context == null ? null : context.getPropertySupplier());
        }
        if (rateLimiterManager == null) {
            rateLimiterManager = new ArchiveRateLimiterManager(context);
        }
        if (sendArchiveService == null) {
            this.sendArchiveService = new ProduceArchiveService(archiveConfig, context, rateLimiterManager);
        }
        if (consumeArchiveService == null) {
            this.consumeArchiveService = new ConsumeArchiveService(archiveConfig, context, rateLimiterManager);
        }
    }

    @Override
    protected void doStart() throws Exception {
        if(archiveConfig.isStartArchive()) {
            super.doStart();
            sendArchiveService.start();
            consumeArchiveService.start();
            logger.info("archive manager started.");
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        Close.close(sendArchiveService);
        Close.close(consumeArchiveService);
        logger.info("archive manager stopped.");
    }

    /**
     * 获取未归档的发送日志条数
     *
     * @return 未归档的发送日志条数
     */
    public long getSendBacklogNum() {
        if (sendArchiveService == null) {
            return 0;
        }
        return sendArchiveService.remainMessagesSum();
    }

    /**
     * 获取剩余未归档消费日志的大小
     *
     * @return 剩余未归档消费日志的大小（文件数量 * 文件大小）
     */
    public long getConsumeBacklogNum() {
        if (consumeArchiveService == null) {
            return 0;
        }
        return consumeArchiveService.getRemainConsumeLogFileNum();
    }

    /**
     * 按主题获取未归档的发送日志条数
     *
     * @return 未归档的发送日志条数
     */
    public Map<String, Long> getSendBacklogNumByTopic() {
        if (sendArchiveService == null) {
            return Collections.emptyMap();
        }
        return sendArchiveService.getArchivePosition();
    }

    public ArchiveConfig getArchiveConfig() {
        return archiveConfig;
    }

    public void setArchiveConfig(ArchiveConfig archiveConfig) {
        this.archiveConfig = archiveConfig;
    }
}
