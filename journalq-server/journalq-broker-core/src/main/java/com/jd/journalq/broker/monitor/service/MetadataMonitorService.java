package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.common.domain.TopicConfig;
import com.jd.journalq.common.response.BooleanResponse;

/**
 * MetadataMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/11
 */
public interface MetadataMonitorService {

    TopicConfig getTopicMetadata(String topic, boolean isCluster);

    BooleanResponse getReadableResult(String topic, String app, String address);

    BooleanResponse getWritableResult(String topic, String app, String address);
}