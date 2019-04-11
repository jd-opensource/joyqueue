/**
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
package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.polling.LongPollingCallback;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.network.command.FetchTopicMessage;
import com.jd.journalq.network.command.FetchTopicMessageAck;
import com.jd.journalq.network.command.FetchTopicMessageAckData;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * FetchTopicMessageLongPollCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class FetchTopicMessageLongPollCallback implements LongPollingCallback {

    protected static final Logger logger = LoggerFactory.getLogger(FetchTopicMessageLongPollCallback.class);

    private FetchTopicMessage fetchTopicMessage;
    private Command request;
    private Transport transport;

    public FetchTopicMessageLongPollCallback(FetchTopicMessage fetchTopicMessage, Command request, Transport transport) {
        this.fetchTopicMessage = fetchTopicMessage;
        this.request = request;
        this.transport = transport;
    }

    @Override
    public void onSuccess(Consumer consumer, PullResult pullResult) throws TransportException {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(pullResult.getBuffers());
        fetchTopicMessageAckData.setCode(pullResult.getJmqCode());

        transport.acknowledge(request, new Command(buildFetchTopicMessageAck(consumer, fetchTopicMessageAckData)));
    }

    @Override
    public void onExpire(Consumer consumer) throws TransportException {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        fetchTopicMessageAckData.setCode(JMQCode.SUCCESS);

        transport.acknowledge(request, new Command(buildFetchTopicMessageAck(consumer, fetchTopicMessageAckData)));
    }

    @Override
    public void onException(Consumer consumer, Throwable throwable) throws TransportException {
        logger.error("fetchTopicMessage longPolling exception, transport: {}, consumer: {}", transport, consumer, throwable);
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        if (throwable instanceof JMQException) {
            fetchTopicMessageAckData.setCode(JMQCode.valueOf(((JMQException) throwable).getCode()));
        } else {
            fetchTopicMessageAckData.setCode(JMQCode.CN_UNKNOWN_ERROR);
        }

        transport.acknowledge(request, new Command(buildFetchTopicMessageAck(consumer, fetchTopicMessageAckData)));
    }

    protected FetchTopicMessageAck buildFetchTopicMessageAck(Consumer consumer, FetchTopicMessageAckData data) {
        Map<String, FetchTopicMessageAckData> dataMap = Maps.newHashMap();
        dataMap.put(consumer.getTopic(), data);

        FetchTopicMessageAck fetchTopicMessageAck = new FetchTopicMessageAck();
        fetchTopicMessageAck.setData(dataMap);
        return fetchTopicMessageAck;
    }
}