package io.chubao.joyqueue.client.internal.transport;

/**
 * ClientConnectionInfo
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class ClientConnectionInfo {

    private String connectionId;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String toString() {
        return "ClientConnectionInfo{" +
                "connectionId='" + connectionId + '\'' +
                '}';
    }
}