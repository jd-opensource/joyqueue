/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.limit.filter;

import org.joyqueue.broker.network.protocol.ProtocolCommandHandlerFilter;
import org.joyqueue.broker.network.traffic.Traffic;
import org.joyqueue.broker.network.traffic.ResponseTrafficPayload;
import org.joyqueue.broker.network.traffic.TrafficType;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.filter.CommandHandlerInvocation;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractLimitFilter
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public abstract class AbstractLimitFilter implements ProtocolCommandHandlerFilter {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractLimitFilter.class);

    @Override
    public Command invoke(CommandHandlerInvocation invocation) throws TransportException {
        Command request = invocation.getRequest();
        Command response = invocation.invoke();

        if (response == null) {
            return response;
        }

        ResponseTrafficPayload trafficPayload = null;
        if (response != null && response.getPayload() instanceof ResponseTrafficPayload) {
            trafficPayload = (ResponseTrafficPayload) response.getPayload();
        } else if (request != null && request.getPayload() instanceof ResponseTrafficPayload) {
            trafficPayload = (ResponseTrafficPayload) request.getPayload();
        }

        if (trafficPayload == null) {
            return response;
        }

        return maybeLimit(invocation.getTransport(), invocation.getRequest(), response, trafficPayload);
    }

    protected Command maybeLimit(Transport transport, Command request, Command response, ResponseTrafficPayload trafficPayload) {
        Traffic traffic = trafficPayload.getTraffic();
        if (!(trafficPayload instanceof TrafficType) || traffic == null) {
            return response;
        }

        if (!limitIfNeeded((TrafficType) trafficPayload, traffic)) {
            return response;
        }

        trafficPayload.onLimited();
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

    protected abstract boolean limitIfNeeded(String topic, String app, String trafficType, Traffic traffic);

    protected abstract Command doLimit(Transport transport, Command request, Command response);
}