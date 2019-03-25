package com.jd.journalq.broker.consumer.model;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.session.Consumer;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by lining48 on 2018/8/16.
 */
public class PullResult {
    // 主题
    private String topic;
    // 应用
    private String app;
    // 分区
    private short partition;
    // 数据
    private List<ByteBuffer> buffers;
    // 状态码
    private JMQCode jmqCode = JMQCode.SUCCESS;

    public PullResult(final Consumer consumer, final short partition, final List<ByteBuffer> buffers) {
        this.topic = consumer.getTopic();
        this.app = consumer.getApp();
        this.partition = partition;
        this.buffers = buffers;
    }


    public PullResult(final String topic, final String app, final short partition, final List<ByteBuffer> buffers) {
        this.topic = topic;
        this.app = app;
        this.partition = partition;
        this.buffers = buffers;
    }


    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }


    public short getPartition() {
        return partition;
    }

    public List<ByteBuffer> getBuffers() {
        return buffers;
    }

    public void setBuffers(List<ByteBuffer> buffers) {
        this.buffers = buffers;
    }

    public JMQCode getJmqCode() {
        return jmqCode;
    }

    public void setJmqCode(JMQCode jmqCode) {
        this.jmqCode = jmqCode;
    }

    /**
     * 转换成缓冲区数组
     *
     * @return 缓冲区数组
     */
    public ByteBuffer[] toArrays() {
        if (buffers == null) {
            return null;
        }
        return buffers.toArray(new ByteBuffer[0]);
    }

    /**
     * 数据条数
     *
     * @return 数据条数
     */
    public int count() {
        if (buffers == null) {
            return 0;
        }
        return buffers.size();
    }

    /**
     * 数据缓冲区大小
     *
     * @return 数据缓冲区大小
     */
    public int size() {
        int len = 0;
        if (buffers != null) {
            for (ByteBuffer buffer : buffers) {
                len += buffer.remaining();
            }
        }
        return len;
    }

    /**
     * 是否为空
     *
     * @return 为空标示
     */
    public boolean isEmpty() {
        return buffers == null || buffers.isEmpty();
    }

}
