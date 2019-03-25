package com.jd.journalq.store;

import com.jd.journalq.exception.JMQCode;

import java.nio.ByteBuffer;


/**
 * 读消息结果
 * 替代 com.jd.journalq.server.store.GetResult
 */
public class ReadResult {
    /**
     * 状态码
     */
    private JMQCode code;

    /**
     * 消息数组
     */
    private ByteBuffer[] messages;

    /**
     * 给定index超过队尾，说明暂时没有消息可以消费了。
     */
    private boolean eop;

    public JMQCode getCode() {
        return code;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public ByteBuffer[] getMessages() {
        return messages;
    }

    public void setMessages(ByteBuffer[] messages) {
        this.messages = messages;
    }

    public boolean isEop() {
        return eop;
    }

    public void setEop(boolean eop) {
        this.eop = eop;
    }
}
