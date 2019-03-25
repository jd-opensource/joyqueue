package com.jd.journalq.client.internal.nameserver;

import com.jd.journalq.toolkit.lang.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * NameServerConfigChecker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class NameServerConfigChecker {

    public static void check(NameServerConfig config) {
        Preconditions.checkArgument(config != null, "nameserver can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getAddress()), "nameserver.address can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getApp()), "nameserver.app can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getToken()), "nameserver.token can not be null");
        Preconditions.checkArgument(config.getUpdateMetadataInterval() > 0, "nameserver.updateMetadataInterval must be greater than 0");
        Preconditions.checkArgument(config.getTempMetadataInterval() > 0, "nameserver.tempMetadataInterval must be greater than 0");
        Preconditions.checkArgument(config.getUpdateMetadataThread() > 0, "nameserver.updateMetadataThread must be greater than 0");
        Preconditions.checkArgument(config.getUpdateMetadataQueueSize() > 0, "nameserver.updateMetadataQueueSize must be greater than 0");
    }
}