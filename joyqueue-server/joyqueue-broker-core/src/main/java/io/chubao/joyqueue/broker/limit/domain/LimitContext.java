package io.chubao.joyqueue.broker.limit.domain;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;

/**
 * LimitContext
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public class LimitContext {

    private Transport transport;
    private Command request;
    private Command response;
    private int delay;

    public LimitContext(Transport transport, Command request, Command response, int delay) {
        this.transport = transport;
        this.request = request;
        this.response = response;
        this.delay = delay;
    }

    public Transport getTransport() {
        return transport;
    }

    public Command getRequest() {
        return request;
    }

    public Command getResponse() {
        return response;
    }

    public int getDelay() {
        return delay;
    }
}