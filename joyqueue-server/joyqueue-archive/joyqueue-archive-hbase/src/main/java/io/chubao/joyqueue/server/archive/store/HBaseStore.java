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
package io.chubao.joyqueue.server.archive.store;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.hbase.HBaseClient;
import io.chubao.joyqueue.server.archive.store.api.ArchiveStore;
import io.chubao.joyqueue.server.archive.store.model.AchivePosition;
import io.chubao.joyqueue.server.archive.store.model.ConsumeLog;
import io.chubao.joyqueue.server.archive.store.model.Query;
import io.chubao.joyqueue.server.archive.store.model.SendLog;
import io.chubao.joyqueue.toolkit.lang.Pair;
import io.chubao.joyqueue.toolkit.network.IpUtil;
import io.chubao.joyqueue.toolkit.security.Md5;
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

import static io.chubao.joyqueue.server.archive.store.HBaseSerializer.byteArrayToHexStr;

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
            if (hBaseClient == null) {
                hBaseClient = new HBaseClient();
            }

            hBaseClient.start();

            topicAppMapping = new HBaseTopicAppMapping(hBaseClient);

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
    public void putConsumeLog(List<ConsumeLog> consumeLogList) throws JoyQueueException {
        List<Pair<byte[], byte[]>> logList = new LinkedList<>();
        try {
            for (ConsumeLog consumeLog : consumeLogList) {
                String app = consumeLog.getApp();
                int appId = topicAppMapping.getAppId(app);
                consumeLog.setAppId(appId);

                Pair<byte[], byte[]> pair = HBaseSerializer.convertConsumeLogToKVBytes(consumeLog);

                logList.add(pair);
            }

            hBaseClient.put(consumeLogTable, cf, col, logList);
        } catch (IOException e) {
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    @Override
    public void putSendLog(List<SendLog> sendLogList) throws JoyQueueException {
        try {
            List<Pair<byte[], byte[]>> logList = new LinkedList<>();
            for (SendLog log : sendLogList) {

                int topicId = topicAppMapping.getTopicId(log.getTopic());
                int appId = topicAppMapping.getAppId(log.getApp());

                log.setTopicId(topicId);
                log.setAppId(appId);

                Pair<byte[], byte[]> pair = HBaseSerializer.convertSendLogToKVBytes(log);
                logList.add(pair);

            }
            // 写HBASE
            hBaseClient.put(sendLogTable, cf, col, logList);
        } catch (Exception e) {
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

            hBaseClient.put(positionTable, cf, col, rowKey, value);
        } catch (IOException e) {
            throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
        }
    }

    @Override
    public Long getPosition(String topic, short partition) throws JoyQueueException {
        try {
            byte[] rowKey = Bytes.toBytes(topic + ":" + partition);
            byte[] bytes = hBaseClient.get(positionTable, cf, col, rowKey);
            if (bytes != null) {
                return Bytes.toLong(bytes);
            } else {
                return null;
            }
        } catch (IOException e) {
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
            List<Pair<byte[], byte[]>> scan = hBaseClient.scan(scanParameters);
            for (Pair<byte[], byte[]> pair : scan) {
                SendLog log = HBaseSerializer.readSendLog(pair);

                log.setClientIpStr(toIpString(log.getClientIp()));

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
        scanParameters.setStartRowKey(createRowKey(queryCondition.getStartRowKey()));
        scanParameters.setStopRowKey(createRowKey(queryCondition.getStopRowKey()));

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
        allocate.putInt(topicId);

        long crateTime = rowKey.getTime();
        allocate.putLong(crateTime);

        String businessId = rowKey.getBusinessId();
        if (businessId != null) {
            allocate.put(Md5.INSTANCE.encrypt(businessId.getBytes(), null));
        }

        String messageId = rowKey.getMessageId();
        if (messageId != null) {
            allocate.put(new BigInteger(messageId, 16).toByteArray());
        } else {
            // 没有messageId填充16个字节
            allocate.put(new byte[16]);
        }

        return allocate.array();
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
            allocate.put(HBaseSerializer.hexStrToByteArray(rowKey.getMessageId()));
            // rowKey
            byte[] bytesRowKey = allocate.array();

            Pair<byte[], byte[]> bytes = hBaseClient.getKV(sendLogTable, cf, col, bytesRowKey);

            SendLog log = HBaseSerializer.readSendLog(bytes);

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

            byte[] messageIdBytes = HBaseSerializer.hexStrToByteArray(messageId);
            scanParameters.setStartRowKey(messageIdBytes);

            ByteBuffer bytebuffer = ByteBuffer.allocate(messageIdBytes.length + 1);
            bytebuffer.put(messageIdBytes);
            bytebuffer.put(endFlag);
            scanParameters.setStopRowKey(bytebuffer.array());

            scanParameters.setRowCount(count);

            List<Pair<byte[], byte[]>> scan = hBaseClient.scan(scanParameters);

            for (Pair<byte[], byte[]> pair : scan) {
                ConsumeLog log = HBaseSerializer.readConsumeLog(pair);
                log.setMessageId(byteArrayToHexStr(log.getBytesMessageId()));

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

}
