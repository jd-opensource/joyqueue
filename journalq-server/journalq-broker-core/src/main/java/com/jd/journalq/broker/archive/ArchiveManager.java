package com.jd.journalq.broker.archive;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (sendArchiveService == null) {
            this.sendArchiveService = new ProduceArchiveService(archiveConfig, context.getClusterManager(), context.getConsume());
        }
        if (consumeArchiveService == null) {
            this.consumeArchiveService = new ConsumeArchiveService(archiveConfig, context.getClusterManager());
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        sendArchiveService.start();
        consumeArchiveService.start();
        logger.info("archive manager started.");
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
        return sendArchiveService.remainMessagesSum();
    }

    /**
     * 获取剩余未归档消费日志的大小
     *
     * @return 剩余未归档消费日志的大小（文件数量 * 文件大小）
     */
    public long getConsumeBacklogNum() {
        return consumeArchiveService.getRemainConsumeLogFileNum();
    }

}
