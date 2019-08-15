package io.chubao.joyqueue.store;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.nio.ByteBuffer;


/**
 * 读消息结果
 */
public class ReadResult {
    /**
     * 状态码
     */
    private JoyQueueCode code;

    /**
     * 消息数组
     */
    private ByteBuffer[] messages;

    /**
     * 给定index超过队尾，说明暂时没有消息可以消费了。
     */
    private boolean eop;

    public JoyQueueCode getCode() {
        return code;
    }

    public void setCode(JoyQueueCode code) {
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
