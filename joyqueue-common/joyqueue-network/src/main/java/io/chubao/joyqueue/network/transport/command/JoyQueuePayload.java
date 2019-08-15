package io.chubao.joyqueue.network.transport.command;

/**
 * JoyQueuePayload
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public abstract class JoyQueuePayload implements Payload, Type {

    private Header header;

    /**
     * 校验
     */
    public void validate() {
        //Do nothing
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }
}