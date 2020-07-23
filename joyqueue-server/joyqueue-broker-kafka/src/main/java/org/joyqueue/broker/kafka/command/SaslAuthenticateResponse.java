package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

/**
 * SaslAuthenticateResponse
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslAuthenticateResponse extends KafkaRequestOrResponse {

    private short errorCode;
    private String errorMessage;
    private byte[] authBytes;
    private long sessionLifeTimeMs;

    public SaslAuthenticateResponse() {

    }

    public SaslAuthenticateResponse(short errorCode, String errorMessage, byte[] authBytes, long sessionLifeTimeMs) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.authBytes = authBytes;
        this.sessionLifeTimeMs = sessionLifeTimeMs;
    }

    public short getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(short errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public byte[] getAuthBytes() {
        return authBytes;
    }

    public void setAuthBytes(byte[] authBytes) {
        this.authBytes = authBytes;
    }

    public long getSessionLifeTimeMs() {
        return sessionLifeTimeMs;
    }

    public void setSessionLifeTimeMs(long sessionLifeTimeMs) {
        this.sessionLifeTimeMs = sessionLifeTimeMs;
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_AUTHENTICATE.getCode();
    }
}
