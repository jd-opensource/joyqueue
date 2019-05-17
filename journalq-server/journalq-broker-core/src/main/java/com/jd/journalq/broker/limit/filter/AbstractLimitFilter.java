package com.jd.journalq.broker.limit.filter;

import com.jd.journalq.broker.network.protocol.ProtocolCommandHandlerFilter;
import com.jd.journalq.broker.network.traffic.Traffic;
import com.jd.journalq.broker.network.traffic.TrafficPayload;
import com.jd.journalq.broker.network.traffic.TrafficType;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.handler.filter.CommandHandlerInvocation;
import com.jd.journalq.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractLimitFilter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public abstract class AbstractLimitFilter implements ProtocolCommandHandlerFilter {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractLimitFilter.class);

    @Override
    public Command invoke(CommandHandlerInvocation invocation) throws TransportException {
        Command response = invocation.invoke();
        if (!isEnable() || response == null || !(response.getPayload() instanceof TrafficPayload)) {
            return response;
        }

        TrafficPayload trafficPayload = (TrafficPayload) response.getPayload();
        return maybeLimit(invocation.getTransport(), invocation.getRequest(), response, trafficPayload);
    }

    protected Command maybeLimit(Transport transport, Command request, Command response, TrafficPayload trafficPayload) {
        if (!(trafficPayload instanceof TrafficType)) {
            return response;
        }

        if (!limitIfNeeded((TrafficType) trafficPayload, trafficPayload.getTraffic())) {
            return response;
        }

        return doLimit(transport, request, response);
    }

    protected boolean limitIfNeeded(TrafficType trafficType, Traffic traffic) {
        String type = trafficType.getTrafficType();
        String app = traffic.getApp();

        for (String topic : traffic.getTopics()) {
            if (limitIfNeeded(topic, app, type, traffic)) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean isEnable();

    protected abstract boolean limitIfNeeded(String topic, String app, String trafficType, Traffic traffic);

    protected abstract Command doLimit(Transport transport, Command request, Command response);
}