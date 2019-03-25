package com.jd.journalq.client.internal.transport.config;

import com.jd.journalq.toolkit.lang.Preconditions;

/**
 * TransportConfigChecker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class TransportConfigChecker {

    public static void check(TransportConfig config) {
        Preconditions.checkArgument(config != null, "transport not null");
        Preconditions.checkArgument(config.getConnections() >= 1, "transport.connections must be greater than 0");
    }
}