package io.chubao.joyqueue.client.internal.trace;

/**
 * TraceCaller
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public interface TraceCaller {

    void end();

    void error();
}