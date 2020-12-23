package org.joyqueue.server.archive.store.utils;

import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.server.archive.store.model.SendLog;
import org.joyqueue.toolkit.lang.Pair;
import org.joyqueue.toolkit.security.Md5;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

public class ArchiveSerializer {

    public static ConsumeLog readConsumeLog(Pair<byte[], byte[]> pair) {
        ConsumeLog log = new ConsumeLog();

        byte[] key = pair.getKey();
        ByteBuffer wrap = ByteBuffer.wrap(key);

        byte[] messageId = new byte[16];
        wrap.get(messageId);
        log.setBytesMessageId(messageId);

        int appId = wrap.getInt();
        log.setAppId(appId);

        byte[] value = pair.getValue();
        ByteBuffer valBF = ByteBuffer.wrap(value);

        log.setBrokerId(valBF.getInt());

        byte[] clientIp = new byte[16];
        valBF.get(clientIp);
        log.setClientIp(clientIp);

        log.setConsumeTime(valBF.getLong());

        return log;
    }

    public static Pair<byte[], byte[]> convertConsumeLogToKVBytes(ConsumeLog consumeLog) {
        ByteBuffer buffer = ByteBuffer.allocate(ConsumeLog.len);
        buffer.put(consumeLog.getBytesMessageId());
        buffer.putInt(consumeLog.getAppId());
        buffer.putInt(consumeLog.getBrokerId());

        byte[] clientIpBytes16 = new byte[16];
        byte[] clientIpBytes = consumeLog.getClientIp();
        System.arraycopy(clientIpBytes, 0, clientIpBytes16,0, Math.min(clientIpBytes.length, clientIpBytes16.length));
        buffer.put(clientIpBytes16);

        buffer.putLong(consumeLog.getConsumeTime());
        buffer.flip();

        byte[] key = new byte[ConsumeLog.keyLen];
        buffer.get(key);

        byte[] value = new byte[ConsumeLog.valLen];
        buffer.get(value);

        return new Pair<>(key, value);
    }

    /**
     * key: topicId(4) + sendTime(8) + businessId(16) + messageId(16) 总长度：44
     * value: brokerId(4) + appId(4) + clientIp(16) + sendTime(8) + compassType(2) + messageBody(变长) + businessId(变长)
     *
     * @param sendLog
     * @return
     */
    public static Pair<byte[], byte[]> convertSendLogToKVBytes(SendLog sendLog) throws GeneralSecurityException {
        ByteBuffer bufferKey = ByteBuffer.allocate(44);
        bufferKey.putInt(sendLog.getTopicId());
        bufferKey.putLong(sendLog.getSendTime());
        bufferKey.put(Md5.INSTANCE.encrypt(sendLog.getBusinessId().getBytes(Charset.forName("utf-8")), null));
        bufferKey.put(md5(sendLog.getMessageId(),null));


        // value
        byte[] messageBody = sendLog.getMessageBody();
        byte[] businessIdBytes = sendLog.getBusinessId().getBytes(Charset.forName("utf-8"));
        int size = 4 + 4 + 16 + 8 + 2 + 4 + messageBody.length + 4 + businessIdBytes.length;
        ByteBuffer bufferVal = ByteBuffer.allocate(size);
        bufferVal.putInt(sendLog.getBrokerId());
        bufferVal.putInt(sendLog.getAppId());

        // clientIP
        byte[] clientIpBytes16 = new byte[16];
        byte[] clientIpBytes = sendLog.getClientIp();
        System.arraycopy(clientIpBytes, 0, clientIpBytes16,0, Math.min(clientIpBytes.length, clientIpBytes16.length));
        bufferVal.put(clientIpBytes16);

        bufferVal.putShort(sendLog.getCompressType());
        bufferVal.putInt(messageBody.length);
        bufferVal.put(messageBody);
        bufferVal.putInt(businessIdBytes.length);
        bufferVal.put(businessIdBytes);

        return new Pair<>(bufferKey.array(), bufferVal.array());
    }

    /**
     * key: topicId(4) + businessId(16) + sendTime(8) + messageId(16) 总长度：44
     * value: brokerId(4) + appId(4) + clientIp(16) + sendTime(8) + compassType(2) + messageBody(变长) + businessId(变长)
     *
     * @param sendLog
     * @return
     */
    public static Pair<byte[], byte[]> convertSendLogToKVBytes4BizId(SendLog sendLog) throws GeneralSecurityException {
        ByteBuffer bufferKey = ByteBuffer.allocate(44);
        bufferKey.putInt(sendLog.getTopicId());
        bufferKey.put(Md5.INSTANCE.encrypt(sendLog.getBusinessId().getBytes(Charset.forName("utf-8")), null));
        bufferKey.putLong(sendLog.getSendTime());
        bufferKey.put(md5(sendLog.getMessageId(), null));


        // value
        byte[] messageBody = sendLog.getMessageBody();
        byte[] businessIdBytes = sendLog.getBusinessId().getBytes(Charset.forName("utf-8"));
        int size = 4 + 4 + 16 + 8 + 2 + 4 + messageBody.length + 4 + businessIdBytes.length;
        ByteBuffer bufferVal = ByteBuffer.allocate(size);
        bufferVal.putInt(sendLog.getBrokerId());
        bufferVal.putInt(sendLog.getAppId());

        // clientIP
        byte[] clientIpBytes16 = new byte[16];
        byte[] clientIpBytes = sendLog.getClientIp();
        System.arraycopy(clientIpBytes, 0, clientIpBytes16,0, Math.min(clientIpBytes.length, clientIpBytes16.length));
        bufferVal.put(clientIpBytes16);

        bufferVal.putShort(sendLog.getCompressType());
        bufferVal.putInt(messageBody.length);
        bufferVal.put(messageBody);
        bufferVal.putInt(businessIdBytes.length);
        bufferVal.put(businessIdBytes);

        return new Pair<>(bufferKey.array(), bufferVal.array());
    }

    public static SendLog readSendLog(Pair<byte[], byte[]> pair) {
        SendLog log = new SendLog();

        byte[] key = pair.getKey();
        ByteBuffer wrap = ByteBuffer.wrap(key);
        // 主题ID
        log.setTopicId(wrap.getInt());
        // 发送时间
        log.setSendTime(wrap.getLong());
        // 业务主键（MD5后的）
        byte[] businessId = new byte[16];
        wrap.get(businessId);
        // 消息ID（MD5后的）
        byte[] messageId = new byte[16];
        wrap.get(messageId);
        log.setBytesMessageId(messageId);
        log.setMessageId(byteArrayToHexStr(messageId));

        byte[] value = pair.getValue();
        ByteBuffer valWrap = ByteBuffer.wrap(value);
        // brokerID
        log.setBrokerId(valWrap.getInt());
        // 应用ID
        log.setAppId(valWrap.getInt());
        // 客户端IP
        byte[] clientIp = new byte[16];
        valWrap.get(clientIp);
        log.setClientIp(clientIp);
        // 压缩类型
        log.setCompressType(valWrap.getShort());
        // 消息体
        int msgBodySize = valWrap.getInt();
        byte[] messageBody = new byte[msgBodySize];
        valWrap.get(messageBody);
        log.setMessageBody(messageBody);
        // 业务主键
        int bizSize = valWrap.getInt();
        byte[] businessIdBytes = new byte[bizSize];
        valWrap.get(businessIdBytes);
        log.setBusinessId(new String(businessIdBytes, Charset.forName("utf-8")));

        return log;
    }

    public static SendLog readSendLog4BizId(Pair<byte[], byte[]> pair) {
        SendLog log = new SendLog();

        byte[] key = pair.getKey();
        ByteBuffer wrap = ByteBuffer.wrap(key);
        // 主题ID
        log.setTopicId(wrap.getInt());
        // 业务主键（MD5后的）
        byte[] businessId = new byte[16];
        wrap.get(businessId);
        // 发送时间
        log.setSendTime(wrap.getLong());
        // 消息ID（MD5后的）
        byte[] messageId = new byte[16];
        wrap.get(messageId);
        log.setBytesMessageId(messageId);
        log.setMessageId(byteArrayToHexStr(messageId));

        byte[] value = pair.getValue();
        ByteBuffer valWrap = ByteBuffer.wrap(value);
        // brokerID
        log.setBrokerId(valWrap.getInt());
        // 应用ID
        log.setAppId(valWrap.getInt());
        // 客户端IP
        byte[] clientIp = new byte[16];
        valWrap.get(clientIp);
        log.setClientIp(clientIp);
        // 压缩类型
        log.setCompressType(valWrap.getShort());
        // 消息体
        int msgBodySize = valWrap.getInt();
        byte[] messageBody = new byte[msgBodySize];
        valWrap.get(messageBody);
        log.setMessageBody(messageBody);
        // 业务主键
        int bizSize = valWrap.getInt();
        byte[] businessIdBytes = new byte[bizSize];
        valWrap.get(businessIdBytes);
        log.setBusinessId(new String(businessIdBytes, Charset.forName("utf-8")));

        return log;
    }

    /**
     * MD5 for content with key
     *
     **/
    public static byte[] md5(String content,byte[] key) throws GeneralSecurityException {
        return Md5.INSTANCE.encrypt(content.getBytes(Charset.forName("utf-8")), key);
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++){
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte)Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static byte[] reverse(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return byteArray;
        }
        byte[] reverseArray = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            reverseArray[i] = byteArray[byteArray.length - i - 1];
        }
        return reverseArray;
    }

    public static byte[] reverse(ByteBuffer buffer) {
        return reverse(buffer.array());
    }

    public static String reverse(String reverseStr) {
        return new StringBuffer(reverseStr).reverse().toString();
    }
}
