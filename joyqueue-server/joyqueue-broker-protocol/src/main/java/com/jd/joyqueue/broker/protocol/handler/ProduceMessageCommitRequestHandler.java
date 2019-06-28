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
package com.jd.joyqueue.broker.protocol.handler;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.cluster.ClusterManager;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.producer.Produce;
import com.jd.joyqueue.broker.protocol.JoyQueueCommandHandler;
import com.jd.joyqueue.broker.protocol.converter.CheckResultConverter;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.exception.JoyQueueException;
import com.jd.joyqueue.message.BrokerCommit;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.command.ProduceMessageCommitRequest;
import com.jd.joyqueue.network.command.ProduceMessageCommitResponse;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.session.Producer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageCommitRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageCommitRequestHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageCommitRequest produceMessageCommitRequest = (ProduceMessageCommitRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageCommitRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessageCommitRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageCommitRequest.getTopic()), produceMessageCommitRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport,
                    produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), checkResult.getJoyQueueCode());
            return new Command(new ProduceMessageCommitResponse(CheckResultConverter.convertCommonCode(checkResult.getJoyQueueCode())));
        }

        ProduceMessageCommitResponse produceMessageCommitResponse = produceMessageCommit(connection, produceMessageCommitRequest);
        return new Command(produceMessageCommitResponse);
    }

    protected ProduceMessageCommitResponse produceMessageCommit(Connection connection, ProduceMessageCommitRequest produceMessageCommitRequest) {
        Producer producer = new Producer(connection.getId(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), Producer.ProducerType.JOYQUEUE);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageCommitRequest.getTopic());
        brokerCommit.setApp(produceMessageCommitRequest.getApp());
        brokerCommit.setTxId(produceMessageCommitRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageCommitResponse(JoyQueueCode.SUCCESS);
        } catch (JoyQueueException e) {
            logger.error("produceMessage commit exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), e);
            return new ProduceMessageCommitResponse(JoyQueueCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage commit exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), e);
            return new ProduceMessageCommitResponse(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_COMMIT_REQUEST.getCode();
    }
}