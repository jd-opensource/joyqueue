package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;

import java.util.Arrays;

/**
 * SaslAuthenticateRequest
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslAuthenticateRequest extends KafkaRequestOrResponse {

    private byte[] authBytes;
    private SaslAuthenticateData data;

    public void setAuthBytes(byte[] authBytes) {
        this.authBytes = authBytes;
    }

    public byte[] getAuthBytes() {
        return authBytes;
    }

    public void setData(SaslAuthenticateData data) {
        this.data = data;
    }

    public SaslAuthenticateData getData() {
        return data;
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_AUTHENTICATE.getCode();
    }

    @Override
    public String toString() {
        return "SaslAuthenticateRequest{" +
                "authBytes=" + Arrays.toString(authBytes) +
                '}';
    }


    public static class SaslAuthenticateData {
        private String app;
        private String token;

        public SaslAuthenticateData() {

        }

        public SaslAuthenticateData(String app, String token) {
            this.app = app;
            this.token = token;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
