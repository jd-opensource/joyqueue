package io.chubao.joyqueue.network.transport.command.provider;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.Transport;

import java.util.concurrent.ExecutorService;

/**
 * ExecutorServiceProvider
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface ExecutorServiceProvider {

    ExecutorService getExecutorService(Transport transport, Command command);
}