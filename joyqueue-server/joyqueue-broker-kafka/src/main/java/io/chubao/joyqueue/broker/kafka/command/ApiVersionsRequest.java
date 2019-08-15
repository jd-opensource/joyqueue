package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;

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