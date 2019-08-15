package io.chubao.joyqueue.network.command;

import java.io.Serializable;

/**
 * TopicPartition
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class TopicPartition implements Serializable {

    private short id;

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TopicPartition{" +
                "id=" + id +
                '}';
    }
}