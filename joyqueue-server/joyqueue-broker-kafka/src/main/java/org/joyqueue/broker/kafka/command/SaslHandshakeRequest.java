package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

/**
 * SaslHandshakeRequest
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslHandshakeRequest extends KafkaRequestOrResponse {

    private String mechanism;

    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }

    public String getMechanism() {
        return mechanism;
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_HANDSHAKE.getCode();
    }
}
