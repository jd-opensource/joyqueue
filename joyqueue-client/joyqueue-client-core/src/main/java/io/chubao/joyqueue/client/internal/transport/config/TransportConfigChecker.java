package io.chubao.joyqueue.client.internal.transport.config;

import com.google.common.base.Preconditions;

/**
 * TransportConfigChecker
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransportConfigChecker {

    public static void check(TransportConfig config) {
        Preconditions.checkArgument(config != null, "transport not null");
        Preconditions.checkArgument(config.getConnections() >= 1, "transport.connections must be greater than 0");
    }
}