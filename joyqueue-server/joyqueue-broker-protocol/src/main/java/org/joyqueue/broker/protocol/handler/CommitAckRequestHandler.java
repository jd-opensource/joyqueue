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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.domain.Partition;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.CommitAckData;
import org.joyqueue.network.command.CommitAckRequest;
import org.joyqueue.network.command.CommitAckResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.RetryType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.toolkit.lang.ListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Commit normal consume acks and retry consume exception acks
 *
 * author: gaohaoxiang
 * date: 2018/12/12
 *
 */
public class CommitAckRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(CommitAckRequestHandler.class);
    private Consume consume;
    private MessageRetry retryManager;
    private ClusterManager clusterManager;
    private static final long warnThresholdBatchSize=4*1024*1024;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.consume = brokerContext.getConsume();
        this.retryManager = brokerContext.getRetryManager();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        CommitAckRequest commitAckRequest = (CommitAckRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);
        if (connection == null || !connection.isAuthorized(commitAckRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, commitAckRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }
        Table<String, Short, JoyQueueCode> result = HashBasedTable.create();
        // First ack normal consume partition, then add consume exception message into retry partition, finally ack exception message
        // one of topic has remaining retry token,continue process retry, else discard the request
        Map<String, Map<Short, List<CommitAckData>>> acks=commitAckRequest.getData().rowMap();
        Map<String, Map<Short, List<CommitAckData>>> consumeExceptionAcks = consumeExceptionAck(acks);
        Map<String, Map<Short, List<CommitAckData>>> normalConsumedAcks= normalConsumeAck(acks,consumeExceptionAcks);
        if(logger.isDebugEnabled()){
            logger.debug("consumer {}, acks {}",connection.getApp(),JSON.toJSONString(acks));
        }
        try {
            // ack consume without exception part
            commitNormalAck(connection,normalConsumedAcks,result);
            if(consumeExceptionAcks.size()>0) {
                List<String> consumeExceptionTopics = Lists.newArrayList(consumeExceptionAcks.keySet());
                if (allRetrySwitchClosed(consumeExceptionTopics, connection.getApp())) {
                    // reply client immediately
                    batchReleaseConsumeLock(connection,consumeExceptionAcks, result);
                } else {
                    // retry and ack consume exception acks
                    boolean batchRetryStatus = batchRetry(connection, consumeExceptionAcks);
                    commitRetryAck(connection, batchRetryStatus, acks, consumeExceptionAcks, result);
                    // should release consumer partition lock
                }
            }
        } catch (JoyQueueException e) {
            if(JoyQueueCode.valueOf(e.getCode())==JoyQueueCode.RETRY_TOKEN_LIMIT){
                // token not available, ignore retry request will lead client retry command timeout
                batchReleaseConsumeLock(connection,consumeExceptionAcks, result);
                if(logger.isDebugEnabled()) {
                    logger.debug("consumer {},topics {} retry too frequency,ignore retry request", connection.getApp(),JSON.toJSONString(consumeExceptionAcks.keySet()));
                }
                return null;
            }
        }
        CommitAckResponse commitAckResponse = new CommitAckResponse();
        commitAckResponse.setResult(result);
        return new Command(commitAckResponse);
    }



    /**
     * Retry service disabled
     *
     **/
    public void batchReleaseConsumeLock(Connection connection,Map<String, Map<Short, List<CommitAckData>>> acks,Table<String, Short, JoyQueueCode> result){
        for(Map.Entry<String,Map<Short, List<CommitAckData>>>  te:acks.entrySet()){
            for(Map.Entry<Short,List<CommitAckData>> partitionAcks:te.getValue().entrySet()){
                Short partition=  partitionAcks.getKey();
                consume.releasePartition(te.getKey(),connection.getApp(),partition);
                result.put(te.getKey(),partition,JoyQueueCode.RETRY_DISABLED);
            }
        }
    }

    /**
     * 提交消费有异常，且已正常进入重试队列的消费位置
     * @param acks  all acks
     * @param consumeExceptionAcks  consume exception acks
     * @param batchRetryStatus  true 表示所有的消费异常都正常进入重试队列
     * @param result  retry result
     **/
    public void commitRetryAck(Connection connection,boolean batchRetryStatus,Map<String, Map<Short, List<CommitAckData>>> acks,
                               Map<String,Map<Short, List<CommitAckData>>> consumeExceptionAcks,Table<String, Short, JoyQueueCode> result){
        for(Map.Entry<String,Map<Short, List<CommitAckData>>> te:consumeExceptionAcks.entrySet()){
            String topic= te.getKey();
            try {
                    boolean retry=retrySwitch(topic, connection.getApp());
                    Map<Short,List<CommitAckData>> partitionAcks=acks.get(topic);
                    for(Map.Entry<Short,List<CommitAckData>> partitionAcksWithRetry:te.getValue().entrySet()) {
                        Short partition = partitionAcksWithRetry.getKey();
                        List<CommitAckData> partitionAckWithRetry = partitionAcks.get(partition);
                        if(batchRetryStatus&&retry) {
                            JoyQueueCode code = commitAck(connection, te.getKey(), connection.getApp(), partition, partitionAckWithRetry);
                            result.put(te.getKey(), partition, code);
                        }else{
                            result.put(te.getKey(), partition,retry?JoyQueueCode.RETRY_ADD:JoyQueueCode.RETRY_DISABLED);
                            consume.releasePartition(topic,connection.getApp(),partition);
                        }
                    }
                }catch (Throwable e) {
                    logger.error(" commit topic:{},app:{},ack:{} exception", topic, connection.getApp(),JSON.toJSON(te), connection.getTransport(), e);
                }
            }
    }

    /**
     * commit normal(without any exception) consume ack
     **/
    public void commitNormalAck(Connection connection,Map<String, Map<Short, List<CommitAckData>>> normalConsumedAcks,Table<String, Short, JoyQueueCode> result){
        for(Map.Entry<String,Map<Short, List<CommitAckData>>>  te:normalConsumedAcks.entrySet()){
            for(Map.Entry<Short,List<CommitAckData>> partitionAcks:te.getValue().entrySet()){
               Short partition=  partitionAcks.getKey();
               JoyQueueCode code=commitAck(connection,te.getKey(),connection.getApp(),partition,partitionAcks.getValue());
               result.put(te.getKey(),partition,code);
            }
        }
    }

    /**
     * @return  true if all retry switch is closed
     **/
    public boolean allRetrySwitchClosed(List<String> topics,String app) throws JoyQueueException{
        for(String topic:topics) {
            if(retrySwitch(topic,app)){
                return false;
            }
        }
        return true;
    }

    /**
     * @return  all consume exception acks
     *
     **/
    public Map<String, Map<Short, List<CommitAckData>>> consumeExceptionAck(Map<String, Map<Short, List<CommitAckData>>> acks){
        Table<String, Short, List<CommitAckData>> result = HashBasedTable.create();
        for(Map.Entry<String,Map<Short, List<CommitAckData>>> topicEntry:acks.entrySet()){
            String topic=topicEntry.getKey();
            for (Map.Entry<Short, List<CommitAckData>> partitionEntry : topicEntry.getValue().entrySet()) {
                Short partition= partitionEntry.getKey();
                for(CommitAckData ack:partitionEntry.getValue()){
                    if(ack.getRetryType()!=RetryType.NONE){
                       List<CommitAckData> partitionExceptionAcks=result.get(topic,partition);
                       if(partitionExceptionAcks==null){
                           partitionExceptionAcks= new ArrayList(partitionEntry.getValue().size());
                           result.put(topic,partition,partitionExceptionAcks);
                       }
                        partitionExceptionAcks.add(ack);
                    }
                }
            }
        }
        return result.rowMap();
    }


    /**
     *  @return  partition consume acks without exception
     *
     **/
    public Map<String, Map<Short, List<CommitAckData>>> normalConsumeAck(Map<String, Map<Short, List<CommitAckData>>> acks,
                                                                         Map<String, Map<Short, List<CommitAckData>>> consumeExceptionAck){
        Table<String, Short, List<CommitAckData>> normalConsumeAcks = HashBasedTable.create();
        for(Map.Entry<String,Map<Short, List<CommitAckData>>> topicEntry:acks.entrySet()){
            String topic=topicEntry.getKey();
            for (Map.Entry<Short, List<CommitAckData>> partitionEntry : topicEntry.getValue().entrySet()) {
                Short partition= partitionEntry.getKey();
                Map<Short,List<CommitAckData>> topicPartitionExceptionAcks=consumeExceptionAck.get(topic);
                if(topicPartitionExceptionAcks==null||topicPartitionExceptionAcks.get(partition)==null){
                    normalConsumeAcks.put(topic,partition,partitionEntry.getValue());
                }
            }
        }
        return normalConsumeAcks.rowMap();
    }

    /**
     *  消费位置 ack
     *
     **/
    protected JoyQueueCode commitAck(Connection connection, String topic, String app, short partition, List<CommitAckData> dataList) {
        if (partition == Partition.RETRY_PARTITION_ID) {
            return doAckRetryPartition(connection, topic, app, partition, dataList);
        } else {
            return doAck(connection, topic, app, partition, dataList);
        }
    }

    /**
     * Consume retry partition ack
     *
     **/
    protected JoyQueueCode doAckRetryPartition(Connection connection, String topic, String app, short partition, List<CommitAckData> acks) {
        try {
            List<Long> retrySuccess = Lists.newLinkedList();
            List<Long> retryError = Lists.newLinkedList();
            for (CommitAckData commitAckData : acks) {
                if(commitAckData.getRetryType().equals(RetryType.NONE)) {
                    retrySuccess.add(commitAckData.getIndex());
                } else {
                    retryError.add(commitAckData.getIndex());
                }
            }
            if (CollectionUtils.isNotEmpty(retrySuccess)) {
                retryManager.retrySuccess(topic, app, ListUtil.toArray(retrySuccess));
            }
            if (CollectionUtils.isNotEmpty(retryError)) {
                retryManager.retryError(topic, app, ListUtil.toArray(retryError));
            }
            return JoyQueueCode.SUCCESS;
        } catch (JoyQueueException e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JoyQueueCode.valueOf(e.getCode());
        } catch (Exception e) {
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return JoyQueueCode.CN_UNKNOWN_ERROR;
        }
    }

    /**
     *   Consume ack, release consume lock if exception
     **/
    public JoyQueueCode doAck(Connection connection, String topic, String app, short partition, List<CommitAckData> acks) {
        MessageLocation[] messageLocations = new MessageLocation[acks.size()];
        Consumer consumer = new Consumer(connection.getId(), topic, app, Consumer.ConsumeType.JOYQUEUE);
        for (int i = 0; i < acks.size(); i++) {
            CommitAckData data = acks.get(i);
            messageLocations[i] = new MessageLocation(topic, partition, data.getIndex());
        }
        try {
            consume.acknowledge(messageLocations, consumer, connection, true);
            return JoyQueueCode.SUCCESS;
        }catch (Throwable e) {
            consume.releasePartition(topic,app,partition);
            JoyQueueCode code=JoyQueueCode.CN_UNKNOWN_ERROR;
            if(e instanceof JoyQueueException){
                code=JoyQueueCode.valueOf(((JoyQueueException) e).getCode());
            }
            logger.error("commit ack exception, topic: {}, app: {}, partition: {}, transport: {}", topic, app, partition, connection.getTransport(), e);
            return code;
        }
    }

    /**
     *  检查重试开关, 消费策略 sequence
     *  @return  重试开关是否打开, 默认 true
     *
     **/
    public boolean retrySwitch(String topic, String app) throws JoyQueueException{
        org.joyqueue.domain.Consumer subscribe = clusterManager.getConsumer(TopicName.parse(topic), app);
        if (subscribe.getConsumerPolicy() != null && (subscribe.getConsumerPolicy().getSeq()||!subscribe.getConsumerPolicy().getRetry())) {
            if(logger.isDebugEnabled()) {
                logger.debug("consumer retry is disabled, sequence:{},retry switch:{},ignore retry, topic: {}, app: {}",
                        subscribe.getConsumerPolicy().getSeq(), subscribe.getConsumerPolicy().getRetry(), topic, app);
            }
            return false;
        }
        return true;
    }


    /**
     *  Batch retry
     *  @param connection  client connection
     *  @param consumeExceptionAcks  消费异常的ack
     *  @return  false if occurs exception
     *  @throws  JoyQueueException  when retry token not available
     **/
    protected boolean batchRetry(Connection connection,Map<String, Map<Short, List<CommitAckData>>> consumeExceptionAcks) throws JoyQueueException {
        try {
            Long batchSize=0L;
            List<RetryMessageModel> retryMessageModels=new ArrayList();
            for (Map.Entry<String, Map<Short, List<CommitAckData>>> te : consumeExceptionAcks.entrySet()) {
                String topic = te.getKey();
                if (retrySwitch(topic, connection.getApp())) {
                    Consumer consumer = new Consumer(connection.getId(), topic, connection.getApp(), Consumer.ConsumeType.JOYQUEUE);
                    for (Map.Entry<Short, List<CommitAckData>> partitionAck : te.getValue().entrySet()) {
                        Short partition = partitionAck.getKey();
                        for (CommitAckData ack : partitionAck.getValue()) {
                            PullResult pullResult = consume.getMessage(topic, partition, ack.getIndex(), 1);
                            List<ByteBuffer> buffers = pullResult.getBuffers();
                            if (buffers.size() != 1) {
                                logger.error("get retryMessage error, message not exist, transport: {}, topic: {}, partition: {}, index: {}",
                                        connection.getTransport().remoteAddress(), consumer.getTopic(), partition, ack.getIndex());
                                continue;
                            }
                            ByteBuffer buffer = buffers.get(0);
                            batchSize += buffer.remaining();
                            BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
                            RetryMessageModel model = generateRetryMessage(consumer, brokerMessage, buffer.array(), ack.getRetryType().name());
                            retryMessageModels.add(model);
                        }
                    }
                }
            }
            // monitor batch size
            if(batchSize>warnThresholdBatchSize){
                logger.warn("topics {}, app {} retry batch size {} too large!",JSON.toJSONString(consumeExceptionAcks.keySet()),connection.getApp(),batchSize);
            }
            if(retryMessageModels.size()>0) {
                retryManager.addRetry(retryMessageModels);
            }
            return true;
        }catch (Throwable e){
            if(e instanceof JoyQueueException){
                JoyQueueException ex=(JoyQueueException)e;
                if(JoyQueueCode.valueOf(ex.getCode())==JoyQueueCode.RETRY_TOKEN_LIMIT){
                    throw ex;
                }
            }
            if(logger.isDebugEnabled()) {
                logger.debug("add retryMessage exception, client transport: {}, app: {},retry:{},{}",
                        connection.getTransport().remoteAddress(), connection.getApp(), JSON.toJSONString(consumeExceptionAcks), e);
            }
        }
        return false;
    }

    private RetryMessageModel generateRetryMessage(Consumer consumer, BrokerMessage brokerMessage, byte[] brokerMessageData/* BrokerMessage 序列化后的字节数组 */, String exception) {
        RetryMessageModel model = new RetryMessageModel();
        model.setBusinessId(brokerMessage.getBusinessId());
        model.setTopic(consumer.getTopic());
        model.setApp(consumer.getApp());
        model.setPartition(Partition.RETRY_PARTITION_ID);
        model.setIndex(brokerMessage.getMsgIndexNo());
        model.setBrokerMessage(brokerMessageData);
        byte[] exceptionBytes = exception.getBytes(Charset.forName("UTF-8"));
        model.setException(exceptionBytes);
        model.setSendTime(brokerMessage.getStartTime());
        return model;
    }


    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_REQUEST.getCode();
    }
}