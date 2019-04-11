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

import com.google.common.collect.Lists;
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
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchProduceFeedback;
import com.jd.journalq.network.command.FetchProduceFeedbackAck;
import com.jd.journalq.network.command.FetchProduceFeedbackAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * FetchProduceFeedbackHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class FetchProduceFeedbackHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchProduceFeedbackHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchProduceFeedback fetchProduceFeedback = (FetchProduceFeedback) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchProduceFeedback.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(fetchProduceFeedback.getTopic()), fetchProduceFeedback.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, fetchProduceFeedback.getTopic(), fetchProduceFeedback.getApp(), checkResult.getJmqCode());
            return new Command(new FetchProduceFeedbackAck(CheckResultConverter.convertCommonCode(checkResult.getJmqCode())));
        }

        FetchProduceFeedbackAck fetchProduceFeedbackAck = FetchProduceFeedback(connection, fetchProduceFeedback);
        return new Command(fetchProduceFeedbackAck);
    }

    protected FetchProduceFeedbackAck FetchProduceFeedback(Connection connection, FetchProduceFeedback fetchProduceFeedback) {
        Producer producer = new Producer(connection.getId(), fetchProduceFeedback.getTopic(), fetchProduceFeedback.getApp(), Producer.ProducerType.JMQ);
        try {
            FetchProduceFeedbackAck fetchProduceFeedbackAck = new FetchProduceFeedbackAck();
            List<TransactionId> transactionIdList = produce.getFeedback(producer, fetchProduceFeedback.getCount());
            fetchProduceFeedbackAck.setData(buildFeedbackAckData(transactionIdList));
            fetchProduceFeedbackAck.setCode(JMQCode.SUCCESS);
            return fetchProduceFeedbackAck;
        } catch (JMQException e) {
            logger.error("fetch feedback exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), fetchProduceFeedback.getTopic(), fetchProduceFeedback.getApp(), e);
            return new FetchProduceFeedbackAck(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetch feedback exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), fetchProduceFeedback.getTopic(), fetchProduceFeedback.getApp(), e);
            return new FetchProduceFeedbackAck(JMQCode.CN_UNKNOWN_ERROR);
        }
    }

    protected List<FetchProduceFeedbackAckData> buildFeedbackAckData(List<TransactionId> transactionIdList) {
        List<FetchProduceFeedbackAckData> result = Lists.newArrayListWithCapacity(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            result.add(new FetchProduceFeedbackAckData(transactionId.getTopic(), transactionId.getTxId(), transactionId.getQueryId()));
        }
        return result;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_PRODUCE_FEEDBACK.getCode();
    }
}