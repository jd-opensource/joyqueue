package com.jd.journalq.network.transport.command.provider;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.Transport;

import java.util.concurrent.ExecutorService;

/**
 * 执行线程提供
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface ExecutorServiceProvider {

    ExecutorService getExecutorService(Transport transport, Command command);
}