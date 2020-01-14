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
package org.joyqueue.broker.consumer.position;

import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.LocalFileStore;
import org.joyqueue.broker.consumer.position.model.Position;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于本地文件存储的 test case
 * <p>
 * Created by chengzhiliang on 2019/3/11.
 */
@Ignore
public class LocalFileStoreTest {

    // 本地文件存储
    private LocalFileStore localFileStore = new LocalFileStore();
    private File base;
    private ConsumePartition consumePartition = new ConsumePartition("topic", "app", (short) 1);
    private Position position = new Position(0, 0, 0, 0);

    @Before
    public void setup() throws Exception {
        final String basePath = "LocalFileTest";
        String property = "java.io.tmpdir";
        String tempDir = System.getProperty(property);
        base = new File(tempDir);
        Assert.assertTrue(base.exists() && base.isDirectory() && base.canWrite());

        deleteBaseFolder();
        base.mkdirs();
        localFileStore.setBasePath(base.getAbsolutePath());
        localFileStore.start();
    }

    @After
    public void deleteBaseFolder() throws IOException {
        localFileStore.stop();
        if (base.exists()) {
            if (base.isDirectory()) deleteFolder(base);
            else base.delete();
        }
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
        put();
        localFileStore.forceFlush();
        ConcurrentMap<ConsumePartition, Position> recover = localFileStore.recover();
        Position positionVal = recover.get(consumePartition);
        Assert.assertEquals(position.toString(), positionVal.toString());
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}