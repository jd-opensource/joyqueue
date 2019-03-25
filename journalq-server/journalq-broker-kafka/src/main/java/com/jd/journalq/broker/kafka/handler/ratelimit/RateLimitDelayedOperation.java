package com.jd.journalq.broker.kafka.handler.ratelimit;

import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.command.KafkaRequestOrResponse;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.toolkit.delay.AbstractDelayedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RateLimitDelayedOperation
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/12
 */
public class RateLimitDelayedOperation extends AbstractDelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(RateLimitDelayedOperation.class);

    private int dealyMs;
    private KafkaCommandHandler delegate;
    private Transport transport;
    private Command request;
    private Command response;

    public RateLimitDelayedOperation(int dealyMs, KafkaCommandHandler delegate, Transport transport, Command request, Command response) {
        super(dealyMs);
        this.dealyMs = dealyMs;
        this.delegate = delegate;
        this.transport = transport;
        this.request = request;
        this.response = response;
    }

    @Override
    protected boolean tryComplete() {
        return false;
    }

    @Override
    protected void onComplete() {
        try {
            if (response != null) {
                if (response.getPayload() instanceof KafkaRequestOrResponse) {
                    ((KafkaRequestOrResponse) response.getPayload()).setThrottleTimeMs((int) dealyMs);
                }
                transport.acknowledge(request, response);
            }
        } catch (Exception e) {
            logger.error("send response failed", e);
        }
    }
}