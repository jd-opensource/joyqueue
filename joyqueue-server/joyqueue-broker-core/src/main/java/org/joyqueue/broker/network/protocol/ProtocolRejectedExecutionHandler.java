package org.joyqueue.broker.network.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ProtocolRejectedExecutionHandler
 * author: gaohaoxiang
 * date: 2020/1/9
 */
public class ProtocolRejectedExecutionHandler implements RejectedExecutionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(ProtocolRejectedExecutionHandler.class);

    private String name;

    public ProtocolRejectedExecutionHandler(String name) {
        this.name = name;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.error("reject {} request, task: {}, rejected from {}", name, r.toString(), executor.toString());
    }
}