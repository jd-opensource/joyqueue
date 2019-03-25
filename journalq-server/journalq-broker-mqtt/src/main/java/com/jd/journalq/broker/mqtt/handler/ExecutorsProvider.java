package com.jd.journalq.broker.mqtt.handler;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public interface ExecutorsProvider {

    public ExecutorService getExecutorService();
}
