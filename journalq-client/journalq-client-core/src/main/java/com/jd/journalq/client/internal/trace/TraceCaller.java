package com.jd.journalq.client.internal.trace;

/**
 * TraceCaller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public interface TraceCaller {

    void end();

    void error();
}