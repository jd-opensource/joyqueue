package com.jd.journalq.registry;

/**
 * 数据节点
 *
 * @author hexiaofeng
 */
public class PathData {

    /**
     * 路径
     */
    private String path;
    /**
     * 数据
     */
    private byte[] data;

    /**
     * 版本
     */
    private int version = -1;

    public PathData(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public PathData(String path, byte[] data, int version) {
        this.path = path;
        this.data = data;
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathData pathData = (PathData) o;
        if (path != null ? !path.equals(pathData.path) : pathData.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }
}
