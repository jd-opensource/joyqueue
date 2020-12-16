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
package org.joyqueue.server.archive.store;

import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.hbase.HBaseClient;
import org.joyqueue.monitor.TraceStat;
import org.joyqueue.server.archive.store.api.ArchiveStore;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.server.archive.store.model.*;
import org.joyqueue.server.archive.store.query.QueryCondition;
import org.joyqueue.server.archive.store.utils.ArchiveSerializer;
import org.joyqueue.toolkit.lang.Pair;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.security.Md5;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

/**
 * 基于Hbase的归档数据访问层
 * <p>
 * Created by chengzhiliang on 2018/9/10.
 */
public class HBaseStore implements ArchiveStore {

    private static final Logger logger = LoggerFactory.getLogger(HBaseStore.class);

    private HBaseClient hBaseClient;
    private HBaseTopicAppMapping topicAppMapping;

    private String consumeLogTable = "consume_log";
    private String sendLogTable = "send_log";
    private String positionTable = "archive_position";
    private byte[] cf = "cf".getBytes(Charset.forName("utf-8"));
    private byte[] col = "col".getBytes(Charset.forName("utf-8"));

    /**
     * 存储命名空间
     */
    private String namespace;

    // start flag
    private boolean isStart = false;

    public HBaseStore() {
    }

    public HBaseStore(HBaseClient hBaseClient) {
        this.hBaseClient = hBaseClient;
    }

    @Override
    public boolean isStarted() {
        return isStart;
    }

    @Override
    public void start() {
        try {
            if (StringUtils.isEmpty(namespace)) {
                logger.error("archive namespace is null.");
            }
            logger.info("archive namespace is [{}]", namespace);

            if (hBaseClient == null) {
                hBaseClient = new HBaseClient();
            }

            hBaseClient.start();

            topicAppMapping = new HBaseTopicAppMapping(namespace, hBaseClient);

            isStart = true;
            logger.info("HBaseStore is started.");
        } catch (Throwable th) {
            isStart = false;
            logger.error(th.getMessage(), th);
        }
    }

    @Override
    public void stop() {
        hBaseClient.stop();
        logger.info("HBaseClient is stopped.");
    }

    @Override
    public void putConsumeLog(List<ConsumeLog> consumeLogList, PointTracer tracer) throws JoyQueueException {
        TraceStat stat = tracer.begin("org.joyqueue.server.archive.store.HBaseStore.putConsumeLog");
        List<Pair<byte[], byte[]>> logList = new LinkedList<>();
        try {
            for (ConsumeLog consumeLog : consumeLogList) {
                String app = consumeLog.getApp();
                int appId = topicAppMapping.getAppId(app);
                consumeLog.setAppId(appId);

                Pair<byte[], byte[]> pair = ArchiveSerializer.convertConsumeLogToKVBytes(consumeLog);

                logList.add(pair);
            }

            hBaseClient.put(namespace, consumeLogTable, cf, col, logList);
            tracer.end(stat);
        } catch (IOException e) {
            tracer.error(stat);
            logger.error("putConsumeLog exception, consumeLogList: {}", consumeLogList, e);
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    @Override
    public void putSendLog(List<SendLog> sendLogList, PointTracer tracer) throws JoyQueueException {
        TraceStat stat = tracer.begin("org.joyqueue.server.archive.store.HBaseStore.putSendLog");
        try {
            List<Pair<byte[], byte[]>> logList = new LinkedList<>();
            for (SendLog log : sendLogList) {

                int topicId = topicAppMapping.getTopicId(log.getTopic());
                int appId = topicAppMapping.getAppId(log.getApp());

                log.setTopicId(topicId);
                log.setAppId(appId);

                Pair<byte[], byte[]> pair = ArchiveSerializer.convertSendLogToKVBytes(log);
                logList.add(pair);

                Pair<byte[], byte[]> pair4BizId = ArchiveSerializer.convertSendLogToKVBytes4BizId(log);
                logList.add(pair4BizId);

            }
            // 写HBASE
            hBaseClient.put(namespace, sendLogTable, cf, col, logList);
            tracer.end(stat);
        } catch (Exception e) {
            tracer.error(stat);
            logger.error("putSendLog exception, sendLogList: {}", sendLogList, e);
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    @Override
    public void putPosition(AchivePosition achivePosition) throws JoyQueueException {
        try {
            String topic = achivePosition.getTopic();
            short partition = achivePosition.getPartition();
            byte[] rowKey = Bytes.toBytes(topic + ":" + partition);
            byte[] value = Bytes.toBytes(achivePosition.getIndex());

            hBaseClient.put(namespace, positionTable, cf, col, rowKey, value);
        } catch (IOException e) {
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    @Override
    public Long getPosition(String topic, short partition) throws JoyQueueException {
        try {
            byte[] rowKey = Bytes.toBytes(topic + ":" + partition);
            byte[] bytes = hBaseClient.get(namespace, positionTable, cf, col, rowKey);
            if (bytes != null) {
                return Bytes.toLong(bytes);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    @Override
    public void cleanPosition(String topic, short partition) throws JoyQueueException {
        try {
          Long currentPosition=  getPosition(topic,partition);
          byte[] rowKey = Bytes.toBytes(topic + ":" + partition);
          hBaseClient.delete(namespace, positionTable, cf, col, rowKey);
          if(currentPosition!=null){
              logger.info("clean topic {}/partition {},archive position {}",topic,partition,currentPosition);
            }else{
              logger.info("clean topic {}/partition {},archive position not init",topic,partition);
          }
        } catch (IOException e) {
            logger.info("clean archive position exception",e);
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    /**
     * 范围查询 左开右闭
     *
     * @param query
     * @return
     * @throws JoyQueueException
     */
    @Override
    public List<SendLog> scanSendLog(Query query) throws JoyQueueException {
        if (hBaseClient == null) {
            logger.error("hBaseClient is null,archive no service");
            throw new JoyQueueException(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE, "hBaseClient is null");
        }
        List<SendLog> logList = new LinkedList<>();
        // 查询发送日志（rowkey=topicId+sendTime+businessId）
        try {
            HBaseClient.ScanParameters scanParameters = buildScanParameters(query);
            List<Pair<byte[], byte[]>> scan = hBaseClient.scan(namespace, scanParameters);

            QueryCondition queryCondition = query.getQueryCondition();
            String businessId = queryCondition.getStartRowKey().getBusinessId();
            boolean hasBizId = StringUtils.isNotEmpty(businessId);
            for (Pair<byte[], byte[]> pair : scan) {
                SendLog log;
                if (hasBizId) {
                    log = ArchiveSerializer.readSendLog4BizId(pair);
                } else {
                    log = ArchiveSerializer.readSendLog(pair);
                }

                log.setClientIpStr(toIpString(log.getClientIp()));
                log.setRowKeyStart(ArchiveSerializer.byteArrayToHexStr(pair.getKey()));
                String topicName = topicAppMapping.getTopicName(log.getTopicId());
                log.setTopic(topicName);

                String appName = topicAppMapping.getAppName(log.getAppId());
                log.setApp(appName);
                logList.add(log);
            }
        } catch (Exception e) {
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }

        return logList;
    }

    /**
     * 转换成IP地址
     *
     * @param clientIpBytes
     * @return
     */
    private String toIpString(byte[] clientIpBytes) {
        int len = clientIpBytes.length;
        if (len != 16) {
            logger.error("Client IP byte array length error.");
        }
        boolean ipv4 = true;
        // 判断是否IPv6字节数组
        for (int i = 6; i < 16; i++) {
            if (clientIpBytes[i] == 0) {
                continue;
            }
            ipv4 = false;
            break;
        }

        StringBuilder clientIp = new StringBuilder();
        if (ipv4) {
            byte[] ipv4Bytes = new byte[6];
            System.arraycopy(clientIpBytes, 0, ipv4Bytes, 0, ipv4Bytes.length);
            IpUtil.toAddress(ipv4Bytes, clientIp);

            return clientIp.toString();
        } else {
            IpUtil.toAddress(clientIpBytes, clientIp);

            return clientIp.toString();
        }
    }

    /**
     * 构建参数
     *
     * @param query
     * @return
     * @throws GeneralSecurityException
     * @throws JoyQueueException
     */
    private HBaseClient.ScanParameters buildScanParameters(Query query) throws GeneralSecurityException, JoyQueueException {
        QueryCondition queryCondition = query.getQueryCondition();

        HBaseClient.ScanParameters scanParameters = new HBaseClient.ScanParameters();
        scanParameters.setTableName(sendLogTable);
        scanParameters.setCf(cf);
        scanParameters.setCol(col);
        scanParameters.setRowCount(queryCondition.getCount());
        byte[] startRowKeyByteArr = queryCondition.getStartRowKeyByteArr();
        if (startRowKeyByteArr != null) {
            scanParameters.setStartRowKey(startRowKeyByteArr);
        } else {
            scanParameters.setStartRowKey(createRowKey(queryCondition.getStartRowKey()));
        }
        scanParameters.setStopRowKey(createRowKey(queryCondition.getStopRowKey()));

        /**
         * cbase性能不满足模糊匹配，所以暂时去掉
         */
        // scanParameters.setFilter(createFilter(queryCondition.getStartRowKey(), createRowKey(queryCondition.getStartRowKey())));

        return scanParameters;
    }

    /**
     * 构建 rowkey
     *
     * @param rowKey
     * @return
     * @throws GeneralSecurityException
     * @throws JoyQueueException
     */
    private byte[] createRowKey(QueryCondition.RowKey rowKey) throws GeneralSecurityException, JoyQueueException {
        // 4 + 8 + 16 + 16
        ByteBuffer allocate = ByteBuffer.allocate(44);

        int topicId = topicAppMapping.getTopicId(rowKey.getTopic());
        long crateTime = rowKey.getTime();
        String businessId = rowKey.getBusinessId();
        String messageId = rowKey.getMessageId();

        allocate.putInt(topicId);
        if (StringUtils.isNotEmpty(businessId)) {
            allocate.put(Md5.INSTANCE.encrypt(businessId.getBytes(), null));
            allocate.putLong(crateTime);
        } else {
            allocate.putLong(crateTime);
            // 没有businessId填充16个字节
            allocate.put(new byte[16]);
        }
        if (messageId != null) {
            allocate.put(new BigInteger(messageId, 16).toByteArray());
        } else {
            // 没有messageId填充16个字节
            allocate.put(new byte[16]);
        }

        return allocate.array();
    }

    private Filter createFilter(QueryCondition.RowKey rowKey, byte[] startRowKey) {
        String businessId = rowKey.getBusinessId();
        if (StringUtils.isNotEmpty(businessId)) {
            List<org.apache.hadoop.hbase.util.Pair<byte[], byte[]>> fuzzyKeysData = new LinkedList<>();
            org.apache.hadoop.hbase.util.Pair<byte[], byte[]> pair = new org.apache.hadoop.hbase.util.Pair<>();

            // 时间任意
            for (int i = 4; i < 12; i++) {
                startRowKey[i] = Bytes.toBytes("?")[0];
            }
            // messageId任意
            for (int i = 28; i < 44; i++) {
                startRowKey[i] = Bytes.toBytes("?")[0];
            }

            pair.setFirst(startRowKey);

            byte fixed = 0x0; //必须匹配
            byte unFixed = 0x1; //不用匹配

            ByteBuffer allocate = ByteBuffer.allocate(44);
            for (int i = 0; i < 4; i++) {
                allocate.put(fixed);
            }
            for (int i = 0; i < 8; i++) {
                allocate.put(unFixed);
            }
            for (int i = 0; i < 16; i++) {
                allocate.put(fixed);
            }
            for (int i = 0; i < 16; i++) {
                allocate.put(unFixed);
            }

            pair.setSecond(allocate.array());

            fuzzyKeysData.add(pair);

            Filter filter = new FuzzyRowFilter(fuzzyKeysData);
            return filter;
        }
        return null;
    }

    @Override
    public SendLog getOneSendLog(Query query) throws JoyQueueException {
        QueryCondition queryCondition = query.getQueryCondition();
        QueryCondition.RowKey rowKey = queryCondition.getRowKey();

        try {
            // 4 + 8 + 16 + 16
            ByteBuffer allocate = ByteBuffer.allocate(44);
            allocate.putInt(topicAppMapping.getTopicId(rowKey.getTopic()));
            allocate.putLong(rowKey.getTime());
            allocate.put(Md5.INSTANCE.encrypt(rowKey.getBusinessId().getBytes(Charset.forName("utf-8")), null));
            allocate.put(ArchiveSerializer.hexStrToByteArray(rowKey.getMessageId()));
            // rowKey
            byte[] bytesRowKey = allocate.array();

            Pair<byte[], byte[]> bytes = hBaseClient.getKV(namespace, sendLogTable, cf, col, bytesRowKey);

            SendLog log = ArchiveSerializer.readSendLog(bytes);

            StringBuilder clientIp = new StringBuilder();
            IpUtil.toAddress(log.getClientIp(), clientIp);
            log.setClientIpStr(clientIp.toString());

            String topicName = topicAppMapping.getTopicName(log.getTopicId());
            log.setTopic(topicName);

            String appName = topicAppMapping.getAppName(log.getAppId());
            log.setApp(appName);

            return log;
        } catch (Exception e) {
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    private static final byte endFlag = 58; // 结束符
    @Override
    public List<ConsumeLog> scanConsumeLog(String messageId, Integer count) throws JoyQueueException {
        if (hBaseClient == null) {
            logger.error("hBaseClient is null,archive no service");
            throw new JoyQueueException(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE, "hBaseClient is null");
        }
        // 查询消费日志(rowkey=messageId+appId)
        List<ConsumeLog> logList = new LinkedList<>();
        // 查询发送日志（rowkey=topicId+sendTime+businessId）
        try {
            HBaseClient.ScanParameters scanParameters = new HBaseClient.ScanParameters();
            scanParameters.setTableName(consumeLogTable);
            scanParameters.setCf(cf);
            scanParameters.setCol(col);

            byte[] messageIdBytes = ArchiveSerializer.hexStrToByteArray(messageId);
            scanParameters.setStartRowKey(messageIdBytes);

            ByteBuffer bytebuffer = ByteBuffer.allocate(messageIdBytes.length + 1);
            bytebuffer.put(messageIdBytes);
            bytebuffer.put(endFlag);
            scanParameters.setStopRowKey(bytebuffer.array());

            scanParameters.setRowCount(count);

            List<Pair<byte[], byte[]>> scan = hBaseClient.scan(namespace, scanParameters);

            for (Pair<byte[], byte[]> pair : scan) {
                ConsumeLog log = ArchiveSerializer.readConsumeLog(pair);
                log.setMessageId(ArchiveSerializer.byteArrayToHexStr(log.getBytesMessageId()));

                StringBuilder clientIp = new StringBuilder();
                IpUtil.toAddress(log.getClientIp(), clientIp);
                log.setClientIpStr(clientIp.toString());

                String appName = topicAppMapping.getAppName(log.getAppId());
                log.setApp(appName);

                logList.add(log);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }

        return logList;
    }

    @Override
    public void setNameSpace(String nameSpace) {
        this.namespace = nameSpace;
    }

}