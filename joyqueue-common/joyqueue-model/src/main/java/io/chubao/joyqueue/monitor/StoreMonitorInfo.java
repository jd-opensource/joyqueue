package io.chubao.joyqueue.monitor;

/**
 * StoreMonitorInfo
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/29
 */
public class StoreMonitorInfo {

    private boolean started;

    private String totalSpace;
    private String freeSpace;

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }

    public String getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(String totalSpace) {
        this.totalSpace = totalSpace;
    }

    public String getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(String freeSpace) {
        this.freeSpace = freeSpace;
    }
}