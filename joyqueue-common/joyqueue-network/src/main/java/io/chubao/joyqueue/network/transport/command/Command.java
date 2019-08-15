package io.chubao.joyqueue.network.transport.command;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 命令
 * Created by hexiaofeng on 16-6-22.
 */
public class Command {
    // 头
    protected Header header;
    // 数据包
    protected Object payload;
    // 是否已经是否了
    protected AtomicBoolean released = new AtomicBoolean(false);
    // 附加属性
    protected Object attachment;

    public Command() {
    }

    public Command(Object payload) {
        this.payload = payload;
    }

    public Command(Header header, Object payload) {
        this.header = header;
        this.payload = payload;
    }

    public Command(Header header, Object payload, Object attachment) {
        this.header = header;
        this.payload = payload;
        this.attachment = attachment;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public Object getAttachment() {
        return attachment;
    }

    public boolean isSuccess() {
        return (header.getStatus() == JoyQueueCode.SUCCESS.getCode());
    }

    public void release() {
        if (payload == null || !(payload instanceof Releasable)) {
            return;
        }
        //TODO
        if (!released.compareAndSet(false, true)) {
            return;
        }
        ((Releasable) payload).release();
    }

    @Override
    public String toString() {
        return String.format("Command:{header:{type:%s, version: %s}, payload: %s}",
                null!=header?header.getType():null, null!=header?header.getVersion():null, payload);
    }

    /**
     * 构造器
     */
    public static class Builder {

        protected Command command = new Command();

        public Builder() {
        }

        public Builder(Command command) {
            this.command = command;
        }

        public static Builder build() {
            return new Builder();
        }

        public static Builder build(final Command command) {
            return new Builder(command);
        }

        public Builder header(final Header header) {
            command.setHeader(header);
            return this;
        }

        public Builder payload(final Object payload) {
            command.setPayload(payload);
            return this;
        }
    }
}
