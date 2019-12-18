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
package io.chubao.joyqueue.broker.protocol.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.protocol.converter.CheckResultConverter;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.message.BrokerPrepare;
import io.chubao.joyqueue.message.SourceType;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessagePrepareRequest;
import io.chubao.joyqueue.network.command.ProduceMessagePrepareResponse;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.network.session.TransactionId;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.response.BooleanResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessagePrepareRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
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
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessagePrepareRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        String producerId = connection.getProducer(produceMessagePrepareRequest.getTopic(), produceMessagePrepareRequest.getApp());
        Producer producer = (StringUtils.isBlank(producerId) ? null : sessionManager.getProducerById(producerId));
        if (producer == null) {
            logger.warn("producer is not exists, transport: {}", transport);
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