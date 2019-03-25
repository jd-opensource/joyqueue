package com.jd.journalq.registry.listener;

/**
 * 选举事件
 */
public class LeaderEvent {

    private LeaderEventType type;
    private String path;

    public LeaderEvent(LeaderEventType type, String path) {
        this.type = type;
        this.path = path;
    }

    /**
     * @return 事件类型
     */
    public LeaderEventType getType() {
        return type;
    }

    /**
     * @return 全路径
     */
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "LeaderEvent [type=" + type + ", path=" + path + "]";
    }

    public enum LeaderEventType {
        /**
         * 取得Leader
         */
        TAKE,
        /**
         * 丢失Leader
         */
        LOST,
    }

}
