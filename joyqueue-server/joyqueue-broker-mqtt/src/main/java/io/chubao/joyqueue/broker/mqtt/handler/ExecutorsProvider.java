package io.chubao.joyqueue.broker.mqtt.handler;

import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public interface ExecutorsProvider {

    ExecutorService getExecutorService();
}
