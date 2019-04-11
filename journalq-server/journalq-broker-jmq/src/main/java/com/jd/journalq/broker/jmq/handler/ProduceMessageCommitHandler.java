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

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerCommit;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessageCommit;
import com.jd.journalq.network.command.ProduceMessageCommitAck;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageCommitHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageCommitHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageCommit produceMessageCommit = (ProduceMessageCommit) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageCommit.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageCommit.getTopic()), produceMessageCommit.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, produceMessageCommit.getTopic(), produceMessageCommit.getApp(), checkResult.getJmqCode());
            return new Command(new ProduceMessageCommitAck(CheckResultConverter.convertCommonCode(checkResult.getJmqCode())));
        }

        ProduceMessageCommitAck produceMessageCommitAck = produceMessageCommit(connection, produceMessageCommit);
        return new Command(produceMessageCommitAck);
    }

    protected ProduceMessageCommitAck produceMessageCommit(Connection connection, ProduceMessageCommit produceMessageCommit) {
        Producer producer = new Producer(connection.getId(), produceMessageCommit.getTopic(), produceMessageCommit.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageCommit.getTopic());
        brokerCommit.setApp(produceMessageCommit.getApp());
        brokerCommit.setTxId(produceMessageCommit.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageCommitAck(JMQCode.SUCCESS);
        } catch (JMQException e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommit.getTopic(), produceMessageCommit.getApp(), e);
            return new ProduceMessageCommitAck(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommit.getTopic(), produceMessageCommit.getApp(), e);
            return new ProduceMessageCommitAck(JMQCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_COMMIT.getCode();
    }
}