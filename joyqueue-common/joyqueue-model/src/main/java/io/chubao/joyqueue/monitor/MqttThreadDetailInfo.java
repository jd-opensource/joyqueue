package io.chubao.joyqueue.monitor;

/**
 * @author majun8
 */
public class MqttThreadDetailInfo extends BaseMonitorInfo {
    private int threadId;
    private int totalClients;
    private boolean isDebug;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(int totalClients) {
        this.totalClients = totalClients;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }
}
