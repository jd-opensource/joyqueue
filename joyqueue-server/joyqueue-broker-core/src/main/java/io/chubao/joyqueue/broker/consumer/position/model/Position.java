package io.chubao.joyqueue.broker.consumer.position.model;

/**
 * 位置信息（消费序号/拉取序号）
 * <p>
 * Created by chengzhiliang on 2019/2/28.
 */
public class Position implements Cloneable {
    // 开始应答序号
    private volatile long ackStartIndex;
    // 结束应答序号
    private volatile long ackCurIndex;
    // 开始拉取序号
    private volatile long pullStartIndex;
    // 结束拉取序号
    private volatile long pullCurIndex;

    public Position() {

    }

    public Position(long ackStartIndex, long ackEndIndex, long pullStartIndex, long pullEndIndex) {
        this.ackStartIndex = ackStartIndex;
        this.ackCurIndex = ackEndIndex;
        this.pullStartIndex = pullStartIndex;
        this.pullCurIndex = pullEndIndex;
    }

    public long getAckStartIndex() {
        return ackStartIndex;
    }

    public void setAckStartIndex(long ackStartIndex) {
        this.ackStartIndex = ackStartIndex;
    }

    public long getAckCurIndex() {
        return ackCurIndex;
    }

    public void setAckCurIndex(long ackCurIndex) {
        this.ackCurIndex = ackCurIndex;
    }

    public long getPullStartIndex() {
        return pullStartIndex;
    }


    public void setPullCurIndex(long pullCurIndex) {
        this.pullCurIndex = pullCurIndex;
    }

    public void setPullStartIndex(long pullStartIndex) {
        this.pullStartIndex = pullStartIndex;
    }

    public long getPullCurIndex() {
        return pullCurIndex;
    }


    @Override
    public Position clone() throws CloneNotSupportedException {
        return (Position) super.clone();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Position{");
        sb.append("ackStartIndex='").append(ackStartIndex).append('\'');
        sb.append(", ackCurIndex=").append(ackCurIndex);
        sb.append(", pullStartIndex=").append(pullStartIndex);
        sb.append(", pullCurIndex=").append(pullCurIndex);
        sb.append('}');
        return sb.toString();
    }
}
