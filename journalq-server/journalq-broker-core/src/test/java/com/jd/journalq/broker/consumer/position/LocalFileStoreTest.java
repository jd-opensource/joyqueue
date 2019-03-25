package com.jd.journalq.broker.consumer.position;

import com.jd.journalq.broker.consumer.model.ConsumePartition;
import com.jd.journalq.broker.consumer.position.model.Position;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于本地文件存储的 test case
 * <p>
 * Created by chengzhiliang on 2019/3/11.
 */
public class LocalFileStoreTest {

    // 本地文件存储
    private LocalFileStore localFileStore = new LocalFileStore();
    private String basePath = "temp/position_store";
    private ConsumePartition consumePartition = new ConsumePartition("topic", "app", (short) 1);
    private Position position = new Position(0, 0, 0, 0);

    @Before
    public void setup() throws Exception {
        localFileStore.setBasePath(basePath);
        localFileStore.start();
    }


    @Test
    public void isStart() {
        boolean started = localFileStore.isStarted();
        Assert.assertEquals(true, started);
    }

    @Test
    public void get() {
        localFileStore.put(consumePartition, position);

        Position position1 = localFileStore.get(consumePartition);

        Assert.assertEquals(position, position1);
    }

    @Test
    public void put() {
        get();
    }

    @Test
    public void remove() {
        localFileStore.put(consumePartition, position);

        Position remove = localFileStore.remove(consumePartition);
        Assert.assertEquals(remove, position);

        Position position = localFileStore.get(consumePartition);
        Assert.assertEquals(null, position);
    }

    @Test
    public void putIfAbsent() {
        put();

        localFileStore.putIfAbsent(consumePartition, new Position(1,1,1,1));

        Position positionVal = localFileStore.get(consumePartition);
        Assert.assertEquals(position, positionVal);
    }

    @Test
    public void forceFlush() {
        put();
        localFileStore.forceFlush();
    }

    @Test
    public void recover() throws IOException {
        ConcurrentMap<ConsumePartition, Position> recover = localFileStore.recover();
        Position positionVal = recover.get(consumePartition);
        Assert.assertEquals(position.toString(), positionVal.toString());
    }

}