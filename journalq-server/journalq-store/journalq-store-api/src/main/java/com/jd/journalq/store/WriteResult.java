package com.jd.journalq.store;

import com.jd.journalq.common.exception.JMQCode;

/**
 * 写消息结果
 */
public class WriteResult {
    private long [] metrics;

    public WriteResult() {}
    public WriteResult(JMQCode code, long [] indices) {
        this. code = code;
        this.indices = indices;
    }
    public WriteResult(JMQCode code, long [] indices, long [] metrics) {
        this. code = code;
        this.indices = indices;
        this.metrics = metrics;
    }
    /**
     * 状态码
     */
    private JMQCode code;

    /**
     * 写入消息的在partition中的序号
     */
    private long [] indices;

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
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
