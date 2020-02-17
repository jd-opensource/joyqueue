package org.joyqueue.broker.network.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
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
    	throw new RejectedExecutionException(String.format("reject %s request, task: %s, rejected from %s",
    			name, r.toString(), executor.toString()));
    }
}