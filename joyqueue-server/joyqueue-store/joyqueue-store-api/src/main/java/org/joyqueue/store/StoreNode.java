package org.joyqueue.store;

/**
 * StoreNode
 * author: gaohaoxiang
 * date: 2020/3/19
 */
public class StoreNode {

    private int id;
    private boolean writable;
    private boolean readable;

    public StoreNode() {

    }

    public StoreNode(int id, boolean writable, boolean readable) {
        this.id = id;
        this.writable = writable;
        this.readable = readable;
    }

    public int getId() {
        return id;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isReadable() {
        return readable;
    }

    @Override
    public String toString() {
        return "StoreNode{" +
                "id=" + id +
                ", writable=" + writable +
                ", readable=" + readable +
                '}';
    }
}