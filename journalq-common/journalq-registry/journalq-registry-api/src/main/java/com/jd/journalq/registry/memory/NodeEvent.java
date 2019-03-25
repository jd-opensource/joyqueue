package com.jd.journalq.registry.memory;

/**
 * 节点事件
 * Created by hexiaofeng on 15-7-10.
 */
public class NodeEvent {
    // 类型
    protected NodeEventType type;
    // 父节点
    protected Node parent;
    // 当前节点
    protected Node node;

    public NodeEvent(NodeEventType type, Node parent, Node node) {
        this.type = type;
        this.parent = parent;
        this.node = node;
    }

    public NodeEventType getType() {
        return type;
    }

    public Node getParent() {
        return parent;
    }

    public Node getNode() {
        return node;
    }


    /**
     * 节点事件类型
     */
    public enum NodeEventType {
        /**
         * 更新
         */
        UPDATE,
        /**
         * 增加
         */
        ADD,
        /**
         * 删除
         */
        DELETE
    }
}
