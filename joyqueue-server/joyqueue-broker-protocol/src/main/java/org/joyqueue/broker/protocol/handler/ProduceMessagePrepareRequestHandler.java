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

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.producer.Produce;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.converter.CheckResultConverter;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerPrepare;
import org.joyqueue.message.SourceType;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.ProduceMessagePrepareRequest;
import org.joyqueue.network.command.ProduceMessagePrepareResponse;
import org.joyqueue.network.protocol.annotation.ProduceHandler;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessagePrepareRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
@ProduceHandler
public class ProduceMessagePrepareRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessagePrepareRequestHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;
    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessagePrepareRequest produceMessagePrepareRequest = (ProduceMessagePrepareRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessagePrepareRequest.getApp())) {
            logger.warn("connection does not exist, transport: {}, app: {}", transport, produceMessagePrepareRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        String producerId = connection.getProducer(produceMessagePrepareRequest.getTopic(), produceMessagePrepareRequest.getApp());
        Producer producer = (StringUtils.isBlank(producerId) ? null : sessionManager.getProducerById(producerId));
        if (producer == null) {
            logger.warn("producer does not exist, transport: {}", transport);
            return BooleanAck.build(JoyQueueCode.FW_PRODUCER_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessagePrepareRequest.getTopic()),
                produceMessagePrepareRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, produceMessagePrepareRequest,
                    produceMessagePrepareRequest.getApp(), checkResult.getJoyQueueCode());
            return new Command(new ProduceMessagePrepareResponse(CheckResultConverter.convertCommonCode(command.getHeader().getVersion(), checkResult.getJoyQueueCode())));
        }

        ProduceMessagePrepareResponse produceMessagePrepareResponse = produceMessagePrepare(producer, connection, produceMessagePrepareRequest);
        return new Command(produceMessagePrepareResponse);
    }

    protected ProduceMessagePrepareResponse produceMessagePrepare(Producer producer, Connection connection, ProduceMessagePrepareRequest produceMessagePrepareRequest) {
        BrokerPrepare brokerPrepare = new BrokerPrepare();
        brokerPrepare.setTopic(produceMessagePrepareRequest.getTopic());
        brokerPrepare.setApp(produceMessagePrepareRequest.getApp());
        brokerPrepare.setQueryId(produceMessagePrepareRequest.getTransactionId());
        brokerPrepare.setTimeout(produceMessagePrepareRequest.getTimeout());
        brokerPrepare.setSource(SourceType.JOYQUEUE.getValue());

        try {
            TransactionId transactionId = produce.putTransactionMessage(producer, brokerPrepare);
            return new ProduceMessagePrepareResponse(transactionId.getTxId(), JoyQueueCode.SUCCESS);
        } catch (JoyQueueException e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessagePrepareRequest.getTopic(), produceMessagePrepareRequest.getApp(), e);
            return new ProduceMessagePrepareResponse(JoyQueueCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessagePrepareRequest.getTopic(), produceMessagePrepareRequest.getApp(), e);
            return new ProduceMessagePrepareResponse(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_PREPARE_REQUEST.getCode();
    }
}
