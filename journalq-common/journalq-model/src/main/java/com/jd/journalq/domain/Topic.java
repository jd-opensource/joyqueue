package com.jd.journalq.domain;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author wylixiaobin
 * Date: 2018/8/17
 */
public class Topic implements Serializable {
    /**
     * 主题名称
     */
    protected TopicName name;
    /**
     * 主题队列数
     */
    protected short partitions;
    /**
     * 主题类型
     */
    protected Type type = Type.TOPIC;

    /**
     * 优先级队列
     */
    protected Set<Short> priorityPartitions = new TreeSet();

    public TopicName getName() {
        return name;
    }

    public void setName(TopicName name) {
        this.name = name;
    }

    public short getPartitions() {
        return partitions;
    }

    public void setPartitions(short partitions) {
        this.partitions = partitions;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Set<Short> getPriorityPartitions() {
        return priorityPartitions;
    }

    public void setPriorityPartitions(Set<Short> priorityPartitions) {
        this.priorityPartitions = priorityPartitions;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o){
            return true;
        }
        if (o == null || !(o instanceof Topic)){
            return false;
        }
        if (name == null || ((Topic) o).getName() == null){
            return false;
        }

        return name.equals(((Topic) o).getName());
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    /**
     * 主题类型
     */
    public enum Type implements Serializable {
        /**
         * 主题
         */
        TOPIC((byte) 0, "普通主题"),
        /**
         * 广播
         */
        @Deprecated
        BROADCAST((byte) 1, "广播"),
        /**
         * 顺序队列
         */
        SEQUENTIAL((byte) 2, "顺序主题");


        private final byte code;
        private final String name;

        Type(byte code, String name) {
            this.code = code;
            this.name = name;
        }

        public byte code() {
            return this.code;
        }


        public String getName() {
            return this.name;
        }


        public static Type valueOf(final byte value) {
            for (Type type : Type.values()) {
                if (value == type.code) {
                    return type;
                }
            }
            return TOPIC;
        }
    }
}
