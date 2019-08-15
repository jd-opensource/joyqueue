package io.chubao.joyqueue.broker.archive;

import io.chubao.joyqueue.server.archive.store.model.ConsumeLog;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Created by chengzhiliang on 2018/12/13.
 */
public class ArchiveSerializerTest {


    @Test
    public void write() {
        ConsumeLog consumeLog = new ConsumeLog();
        consumeLog.setApp("app_test");
        consumeLog.setBrokerId(1);
        consumeLog.setClientIp(new byte[16]);
        consumeLog.setBytesMessageId(new byte[16]);
        consumeLog.setConsumeTime(SystemClock.now());

        System.out.println(ToStringBuilder.reflectionToString(consumeLog));

        ByteBuffer write = ArchiveSerializer.write(consumeLog);
        System.out.println(write.limit());
    }

    @Test
    public void read() {
        ConsumeLog consumeLog = new ConsumeLog();
        consumeLog.setApp("app_test");
        consumeLog.setBrokerId(1);
        consumeLog.setClientIp(new byte[16]);
        consumeLog.setBytesMessageId(new byte[16]);
        consumeLog.setConsumeTime(SystemClock.now());
        System.out.println(ToStringBuilder.reflectionToString(consumeLog));

        ByteBuffer write = ArchiveSerializer.write(consumeLog);

        int readLen = write.getInt();
        System.out.println(readLen);
        ConsumeLog log = ArchiveSerializer.read(write);
        System.out.println(ToStringBuilder.reflectionToString(log));

    }
}