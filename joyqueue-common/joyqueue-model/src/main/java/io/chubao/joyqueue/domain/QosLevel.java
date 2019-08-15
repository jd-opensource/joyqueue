package io.chubao.joyqueue.domain;


/**
 * 服务水平
 */
public enum QosLevel {


    /**
     * 写入Qos级别：
     * ONE_WAY: 客户端单向向Broker发送消息，无应答；
     * RECEIVE: Broker收到消息后应答；
     * PERSISTENCE：Broker将消息写入磁盘后应答；
     * REPLICATION：Broker将消息复制到集群大多数节点后应答，默认值；
     */
    ONE_WAY(0),
    RECEIVE(1),
    PERSISTENCE(2),
    REPLICATION(3);

    private int value;

    QosLevel(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static QosLevel valueOf(final int value) {
        switch (value) {
            case 0:
                return ONE_WAY;
            case 1:
                return RECEIVE;
            case 2:
                return PERSISTENCE;
            default:
                return REPLICATION;
        }
    }
}
