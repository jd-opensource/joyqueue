package org.joyqueue.store.event;

import org.joyqueue.store.StoreNodes;

/**
 * StoreNodeChangeEvent
 * author: gaohaoxiang
 * date: 2020/3/20
 */
public class StoreNodeChangeEvent extends StoreNodeEvent {

    private String topic;
    private int group;
    private StoreNodes nodes;

    public StoreNodeChangeEvent() {

    }

    public StoreNodeChangeEvent(String topic, int group, StoreNodes nodes) {
        this.topic = topic;
        this.group = group;
        this.nodes = nodes;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public StoreNodes getNodes() {
        return nodes;
    }

    public void setNodes(StoreNodes nodes) {
        this.nodes = nodes;
    }
}