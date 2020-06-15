package com.jd.joyqueue.broker.jmq2.command;

import com.google.common.base.Preconditions;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import org.joyqueue.network.session.ConnectionId;
import org.joyqueue.network.session.Language;

/**
 * 添加连接
 */
public class AddConnection extends JMQ2Payload {
    // 用户
    private String user;
    // 密码
    private String password;
    // 应用
    private String app;
    // 版本
    private String clientVersion;
    // 语言
    private Language language = Language.JAVA;
    // 连接ID
    private ConnectionId connectionId;

    public AddConnection() {
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public AddConnection user(final String user) {
        setUser(user);
        return this;
    }

    public AddConnection password(final String password) {
        setPassword(password);
        return this;
    }

    public AddConnection app(final String app) {
        setApp(app);
        return this;
    }

    public AddConnection clientVersion(final String version) {
        setClientVersion(version);
        return this;
    }

    public AddConnection language(final Language language) {
        setLanguage(language);
        return this;
    }

    public AddConnection connectionId(final ConnectionId connectionId) {
        setConnectionId(connectionId);
        return this;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public void setConnectionId(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(user != null && !user.isEmpty(), " user can not be null");
        Preconditions.checkArgument(password != null && !password.isEmpty(), " password can not be null");
        Preconditions.checkArgument(app != null && !app.isEmpty(), " app can not be null");
        Preconditions.checkArgument(clientVersion != null && !clientVersion.isEmpty(), " clientVersion can not be null");
        Preconditions.checkArgument(language != null, " clientVersion can not be null");
    }

    @Override
    public int type() {
        return JMQ2CommandType.ADD_CONNECTION.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddConnection{");
        sb.append("user='").append(user).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", app='").append(app).append('\'');
        sb.append(", clientVersion='").append(clientVersion).append('\'');
        sb.append(", language=").append(language);
        sb.append(", connectionId=").append(connectionId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddConnection that = (AddConnection) o;

        if (app != null ? !app.equals(that.app) : that.app != null) {
            return false;
        }
        if (connectionId != null ? !connectionId.equals(that.connectionId) : that.connectionId != null) {
            return false;
        }
        if (language != that.language) {
            return false;
        }
        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (app != null ? app.hashCode() : 0);
        result = 31 * result + (clientVersion != null ? clientVersion.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (connectionId != null ? connectionId.hashCode() : 0);
        return result;
    }

}