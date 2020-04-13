package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

import java.util.List;

/**
 * SaslHandshakeResponse
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslHandshakeResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private List<String> mechanisms;

    public SaslHandshakeResponse() {

    }

    public SaslHandshakeResponse(short errorCode, List<String> mechanisms) {
        this.errorCode = errorCode;
        this.mechanisms = mechanisms;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public List<String> getMechanisms() {
        return mechanisms;
    }

    public void setMechanisms(List<String> mechanisms) {
        this.mechanisms = mechanisms;
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_HANDSHAKE.getCode();
    }
}
