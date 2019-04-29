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
package com.jd.journalq.broker;

/**
 * Created by chengzhiliang on 2018/12/13.
 */
// FIXME: 单元测试未通过
public class HBaseStoreTest {

//    HBaseClient client = new HBaseClient("hBase-client-config.xml");
//    HBaseTopicAppMapping topicAppMapping = new HBaseTopicAppMapping(client);
//
//    HBaseStore hBaseStore = new HBaseStore();
//
//    @Test
//    public void putMessages() throws JournalqException {
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
//    public void putConsumeLogs() throws GeneralSecurityException, JournalqException {
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
//            System.out.println(ToStringBuilder.reflectionToString(consumeLog));
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
//    public void readPosition() throws JournalqException {
//        for (int i = 0; i < 3; i++) {
//            Long position = hBaseStore.getPosition("default.topic_test", (short) i);
//            System.out.println(position);
//        }
//
//    }


}