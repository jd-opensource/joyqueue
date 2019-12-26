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
package org.joyqueue.broker.archive.store.hbase;

/**
 * Created by chengzhiliang on 2018/12/13.
 */
//FIXME: 单元测试未通过
public class HBaseStoreTest {

//    static HBaseClient client = new HBaseClient("cbase-client-dev.xml");
//    HBaseTopicAppMapping topicAppMapping = new HBaseTopicAppMapping(client);
//
//    HBaseStore hBaseStore = new HBaseStore(client);
//
//    @Before
//    public void init() throws Exception {
//        hBaseStore.start();
//    }
//
//    @Test
//    public void putMessages() throws JoyQueueException {
//        hBaseStore.putSendLog(getSendList(1));
//    }
//
//
//    /**
//     * 读取
//     *
//     *     private String topic;
//     *     private long sendTime;
//     *     private String businessId;
//     *     private String messageId;
//     *     private int brokerId;
//     *     private String app;
//     *     private byte[] clientIp;
//     *     private short compressType;
//     *     private byte[] messageBody;
//     * @return
//     */
//    private List<SendLog> getSendList(int count) {
//        LinkedList<SendLog> sendLogs = Lists.newLinkedList();
//        for (int i = 0; i < count; i++) {
//            SendLog sendLog = new SendLog();
//            sendLog.setTopic("topic_test");
//            sendLog.setSendTime(1545213887728l + i); // 1545213887728
//            sendLog.setBusinessId("businessId");
//            sendLog.setMessageId("topic_test" + "0" + i);
//            sendLog.setBrokerId(Integer.MAX_VALUE);
//            sendLog.setApp("app_test");
//            sendLog.setClientIp(IpUtil.toByte((new InetSocketAddress(50088))));
//            sendLog.setCompressType((short)1);
//            sendLog.setMessageBody("this is message body".getBytes(Charset.forName("utf-8")));
//
//            sendLogs.add(sendLog);
//        }
//
//        return sendLogs;
//    }
//
//    @Test
//    public void putConsumeLogs() throws GeneralSecurityException, JoyQueueException {
//        List<ConsumeLog> consumeList = getConsumeList(1);
//        hBaseStore.putConsumeLog(consumeList);
//    }
//
//
//    /**
//     *
//     *     private byte[] bytesMessageId; // MD5(topic+partition+index)
//     *     private int appId;
//     *     private int brokerId;
//     *     private byte[] clientIp;
//     *     private long consumeTime;
//     *
//     * @param count
//     * @return
//     */
//    public List<ConsumeLog> getConsumeList(int count) throws GeneralSecurityException {
//        LinkedList<ConsumeLog> consumeLogs = Lists.newLinkedList();
//        for (int i = 0; i < count; i++) {
//            ConsumeLog consumeLog = new ConsumeLog();
//            String messageId = "topic_test" + "0" + i;
//            consumeLog.setBytesMessageId(Md5.INSTANCE.encrypt(messageId.getBytes(Charset.forName("utf-8")), null));
//            consumeLog.setAppId(3);
//            consumeLog.setBrokerId(Integer.MAX_VALUE);
//            consumeLog.setClientIp(IpUtil.toByte((new InetSocketAddress(50088))));
//            consumeLog.setConsumeTime(SystemClock.now());
//
//            consumeLogs.add(consumeLog);
//        }
//
//        return consumeLogs;
//    }
//
//    @Test
//    public void scanConsumeLog() throws IOException {
//        HBaseClient.ScanParameters scanParameters = new HBaseClient.ScanParameters();
//        scanParameters.setTableName("consume_log");
//        scanParameters.setCf("cf".getBytes("utf-8"));
//        scanParameters.setCol("col".getBytes("utf-8"));
//        scanParameters.setStartRowKey(Bytes.toBytes(0));
//        scanParameters.setRowCount(1000);
//        List<Pair<byte[], byte[]>> scan = client.scan(scanParameters);
//        System.out.println(scan.size());
//        for (Pair<byte[], byte[]> pair : scan) {
//            ConsumeLog consumeLog = HBaseSerializer.readConsumeLog(pair);
//            System.out.println(byteArrayToHexStr(consumeLog.getBytesMessageId()));
////            System.out.println(ToStringBuilder.reflectionToString(consumeLog));
//        }
//
//    }
//
//    @Test
//    public void scanSendLog() throws IOException {
//        HBaseClient.ScanParameters scanParameters = new HBaseClient.ScanParameters();
//        scanParameters.setTableName("send_log");
//        scanParameters.setCf("cf".getBytes("utf-8"));
//        scanParameters.setCol("col".getBytes("utf-8"));
//        scanParameters.setStartRowKey(Bytes.toBytes(0));
//        scanParameters.setRowCount(1000);
//        List<Pair<byte[], byte[]>> scan = client.scan(scanParameters);
//        System.out.println(scan.size());
//        for (Pair<byte[], byte[]> pair : scan) {
//            SendLog sendLog = HBaseSerializer.readSendLog(pair);
//            System.out.println(ToStringBuilder.reflectionToString(sendLog));
//        }
//    }
//
//    @Test
//    public void readWrite() throws IOException {
//        File file = new File("/Users/chengzhiliang/temp/test.file");
//        RandomAccessFile raf = new RandomAccessFile(file, "rw");
//        FileChannel channel = raf.getChannel();
//        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 1014);
//        map.put("String".getBytes("utf-8"));
//
//
//        RandomAccessFile rafRead = new RandomAccessFile(file, "r");
//        FileChannel channelRead = raf.getChannel();
//        MappedByteBuffer mapRead = channel.map(FileChannel.MapMode.READ_ONLY, 0, 1014);
//        byte[] bytes = "String".getBytes("utf-8");
//        byte[] bytesResult = new byte[bytes.length];
//        mapRead.get(bytesResult);
//
//        String s = new String(bytesResult, Charset.forName("utf-8"));
//        System.out.println(s);
//    }
//
//    @Test
//    public void readPosition() throws JoyQueueException {
//        for (int i = 0; i < 3; i++) {
//            Long position = hBaseStore.getPosition("default.topic_test", (short) i);
//            System.out.println(position);
//        }
//
//    }
//
//    @Test
//    public void testScan() throws Exception{
//        String messageId  = "FB4B02E86BF356806FD8B02288516E0B";
//        List<ConsumeLog> consumeLogs = hBaseStore.scanConsumeLog(messageId, 1000);
//        System.out.println("consumeLogs size:" + consumeLogs.size());
//        for (ConsumeLog consumeLog : consumeLogs) {
//            System.out.println(ToStringBuilder.reflectionToString(consumeLog));
//        }
//    }
//
//
//    @Test
//    public void timeConvert() {
////        long time = 1550484868098l;
////        Date date = new Date(time);
////        System.out.println(date);
//
//        byte[] arr = {-5,75,2,-24,107,-13,86,-128,111,-40,-80,34,-120,81,110,11};
//        String s = HBaseSerializer.byteArrayToHexStr(arr);
//        System.out.println(s);
//
//    }
//
//    @Test
//    public void getOneSendLog() throws Exception {
//        byte[] key = {0, 0, 0, 13, 0, 0, 1, 105, 72, 16, -56, 55, -73, -81, 7, 31, 95, -105, 105, -108, -19, 89, -28, -49, -121, -49, -21, -12, -74, 28, 60, -100, 4, 69, -55, 8, 109, -26, -66, 126, 3, 14, 72, 22};
//        byte[] value = client.get("send_log", Bytes.toBytes("cf"), Bytes.toBytes("col"), key);
//
//        SendLog sendLog = HBaseSerializer.readSendLog(new Pair<>(key, value));
//        System.out.println(sendLog.getTopic());
//        BrokerMessage brokerMessage = Serializer.readBrokerMessage(ByteBuffer.wrap(sendLog.getMessageBody()));
//        System.out.println(brokerMessage);
//    }

}