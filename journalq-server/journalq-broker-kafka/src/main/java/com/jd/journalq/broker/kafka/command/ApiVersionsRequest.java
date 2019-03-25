package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.broker.kafka.KafkaCommandType;

/**
 * ApiVersionsRequest
 *
 * @author luoruiheng
 * @since 1/5/18
 */
public class ApiVersionsRequest extends KafkaRequestOrResponse {

    @Override
    public int type() {
        return KafkaCommandType.API_VERSIONS.getCode();
    }
}