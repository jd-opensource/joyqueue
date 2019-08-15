package io.chubao.joyqueue.store;

import io.chubao.joyqueue.exception.JoyQueueCode;

/**
 * 写消息结果
 */
public class WriteResult {
    private long [] metrics;

    public WriteResult() {}
    public WriteResult(JoyQueueCode code, long [] indices) {
        this. code = code;
        this.indices = indices;
    }
    public WriteResult(JoyQueueCode code, long [] indices, long [] metrics) {
        this. code = code;
        this.indices = indices;
        this.metrics = metrics;
    }
    /**
     * 状态码
     */
    private JoyQueueCode code;

    /**
     * 写入消息的在partition中的序号
     */
    private long [] indices;

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public long[] getIndices() {
        return indices;
    }

    public void setIndices(long[] indices) {
        this.indices = indices;
    }

    public long[] getMetrics() {
        return metrics;
    }

    public void setMetrics(long[] metrics) {
        this.metrics = metrics;
    }
}
