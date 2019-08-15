package io.chubao.joyqueue.model.domain;

public class Client {
    private static final long serialVersionUID = -2261906559879545877L;
    // 连接ID
    private String connectionId;
    // 应用
    private String app;
    // 版本号
    private String version;
    // 语言
    private String language;
    // 客户端地址
    private String ip;
    // 客户端端口
    private int port;
    // 是否是生产者
    private boolean producerRole = false;
    // 是否是消费者
    private boolean consumerRole = false;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isProducerRole() {
        return producerRole;
    }

    public void setProducerRole(boolean producerRole) {
        this.producerRole = producerRole;
    }

    public boolean isConsumerRole() {
        return consumerRole;
    }

    public void setConsumerRole(boolean consumerRole) {
        this.consumerRole = consumerRole;
    }
}
