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
package org.joyqueue.broker.protocol.handler;

import com.google.common.collect.Maps;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.polling.LongPollingCallback;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.command.FetchTopicMessageAckData;
import org.joyqueue.network.command.FetchTopicMessageRequest;
import org.joyqueue.network.command.FetchTopicMessageResponse;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * FetchTopicMessageLongPollCallback
 *
 * author: gaohaoxiang
 * date: 2019/1/8
 */
public class FetchTopicMessageLongPollCallback implements LongPollingCallback {

    protected static final Logger logger = LoggerFactory.getLogger(FetchTopicMessageLongPollCallback.class);

    private FetchTopicMessageRequest fetchTopicMessageRequest;
    private Command request;
    private Transport transport;

    public FetchTopicMessageLongPollCallback(FetchTopicMessageRequest fetchTopicMessageRequest, Command request, Transport transport) {
        this.fetchTopicMessageRequest = fetchTopicMessageRequest;
        this.request = request;
        this.transport = transport;
    }

    @Override
    public void onSuccess(Consumer consumer, PullResult pullResult) throws TransportException {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(pullResult.getBuffers());
        fetchTopicMessageAckData.setCode(pullResult.getCode());

        transport.acknowledge(request, new Command(buildFetchTopicMessageAck(consumer, fetchTopicMessageAckData)));
    }

    @Override
    public void onExpire(Consumer consumer) throws TransportException {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        fetchTopicMessageAckData.setCode(JoyQueueCode.SUCCESS);

        transport.acknowledge(request, new Command(buildFetchTopicMessageAck(consumer, fetchTopicMessageAckData)));
    }

    @Override
    public void onException(Consumer consumer, Throwable throwable) throws TransportException {
        logger.error("fetchTopicMessage longPolling exception, transport: {}, consumer: {}", transport, consumer, throwable);
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        if (throwable instanceof JoyQueueException) {
            fetchTopicMessageAckData.setCode(JoyQueueCode.valueOf(((JoyQueueException) throwable).getCode()));
        } else {
            fetchTopicMessageAckData.setCode(JoyQueueCode.CN_UNKNOWN_ERROR);
        }

        transport.acknowledge(request, new Command(buildFetchTopicMessageAck(consumer, fetchTopicMessageAckData)));
    }

    protected FetchTopicMessageResponse buildFetchTopicMessageAck(Consumer consumer, FetchTopicMessageAckData data) {
        Map<String, FetchTopicMessageAckData> dataMap = Maps.newHashMap();
        dataMap.put(consumer.getTopic(), data);

        FetchTopicMessageResponse fetchTopicMessageResponse = new FetchTopicMessageResponse();
        fetchTopicMessageResponse.setData(dataMap);
        return fetchTopicMessageResponse;
    }
}