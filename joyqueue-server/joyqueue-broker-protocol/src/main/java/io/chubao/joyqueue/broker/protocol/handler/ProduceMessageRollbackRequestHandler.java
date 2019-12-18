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
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.message.BrokerCommit;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessageRollbackRequest;
import io.chubao.joyqueue.network.command.ProduceMessageRollbackResponse;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageRollbackRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageRollbackRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageRollbackRequestHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageRollbackRequest produceMessageRollbackRequest = (ProduceMessageRollbackRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageRollbackRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessageRollbackRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageRollbackRequest.getTopic()), produceMessageRollbackRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport,
                    produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), checkResult.getJoyQueueCode());
            return new Command(new ProduceMessageRollbackResponse(CheckResultConverter.convertCommonCode(command.getHeader().getVersion(), checkResult.getJoyQueueCode())));
        }

        ProduceMessageRollbackResponse produceMessageRollbackResponse = produceMessageRollback(connection, produceMessageRollbackRequest);
        return new Command(produceMessageRollbackResponse);
    }

    protected ProduceMessageRollbackResponse produceMessageRollback(Connection connection, ProduceMessageRollbackRequest produceMessageRollbackRequest) {
        Producer producer = new Producer(connection.getId(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), Producer.ProducerType.JOYQUEUE);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageRollbackRequest.getTopic());
        brokerCommit.setApp(produceMessageRollbackRequest.getApp());
        brokerCommit.setTxId(produceMessageRollbackRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageRollbackResponse(JoyQueueCode.SUCCESS);
        } catch (JoyQueueException e) {
            logger.error("produceMessage rollback exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), e);
            return new ProduceMessageRollbackResponse(JoyQueueCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage rollback exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), e);
            return new ProduceMessageRollbackResponse(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_ROLLBACK_REQUEST.getCode();
    }
}