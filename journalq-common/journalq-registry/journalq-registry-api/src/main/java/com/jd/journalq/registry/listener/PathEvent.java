package com.jd.journalq.registry.listener;

/**
 * 路径事件
 */
public class PathEvent {

    private PathEventType type;
    private String path;
    private byte[] data;
    private int version = -1;

    public PathEvent(PathEventType type, String path, byte[] data) {
        this.type = type;
        this.path = path;
        this.data = data;
    }

    public PathEvent(PathEventType type, String path, byte[] data, int version) {
        this.type = type;
        this.path = path;
        this.data = data;
        this.version = version;
    }

    /**
     * @return 事件类型
     */
    public PathEventType getType() {
        return type;
    }

    /**
     * @return 全路径
     */
    public String getPath() {
        return path;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PathEvent [type=" + type + ", path=" + path + ", data=" + ((data == null || data.length < 1) ? "" :
                new String(
                data)) + "]";
    }

    public enum PathEventType {
        CREATED, REMOVED, UPDATED
    }

}
