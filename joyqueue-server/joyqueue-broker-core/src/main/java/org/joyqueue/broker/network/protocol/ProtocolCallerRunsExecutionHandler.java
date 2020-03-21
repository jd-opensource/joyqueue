package org.joyqueue.broker.network.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author LiYue
 * Date: 2020/3/21
 */
public class ProtocolCallerRunsExecutionHandler implements RejectedExecutionHandler {
    protected static final Logger logger = LoggerFactory.getLogger(ProtocolRejectedExecutionHandler.class);

    private final String name;

    public ProtocolCallerRunsExecutionHandler(String name) {
        this.name = name;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        r.run();
        logger.warn("Executor {} queue is full, executing task {} in {} netty IO thread.", executor.toString(), r.toString(), name);
        throw new RejectedExecutionException(String.format("reject %s request, task: %s, rejected from %s",
                name, r.toString(), executor.toString()));

    }
}
