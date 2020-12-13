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
package org.joyqueue.broker.archive;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.archive.store.hbase.MockArchiveStore;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.config.Configuration;
import org.joyqueue.domain.Broker;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.server.archive.store.api.ArchiveStore;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chengzhiliang on 2018/12/19.
 */
public class ConsumeArchiveServiceTest {

    final int writeRecordNum = 900000;
    private AtomicInteger readByteCounter = new AtomicInteger(0);
    private ClusterManager clusterManager;
    private ArchiveConfig archiveConfig;
    private ConsumeArchiveService consumeService;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        PropertySupplier propertySupplier = new Configuration();
        ((Configuration) propertySupplier).addProperty(Property.APPLICATION_DATA_PATH, "/Users/majun8");
        BrokerContext brokerContext = new BrokerContext().propertySupplier(propertySupplier);
        clusterManager = new ClusterManager(null, null, null, brokerContext);
        Broker broker = new Broker();
        broker.setId(123);
        Field brokerField = clusterManager.getClass().getDeclaredField("broker");
        brokerField.setAccessible(true);
        brokerField.set(clusterManager, broker);

        archiveConfig = new ArchiveConfig(propertySupplier);
        consumeService = new ConsumeArchiveService(archiveConfig, clusterManager);
    }

    @Test
    public void writeConsumeLog() throws InterruptedException {
        String testPath = getTestPath();
        delTestFolder(testPath);
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = consumeService.new ArchiveMappedFileRepository(testPath);
        String[] appArr = {"test", "app_test", "app_app_test"};
        for (int i = 0; i < writeRecordNum; i++) {
            ConsumeLog consumeLog = new ConsumeLog();
            consumeLog.setBrokerId(i);
            consumeLog.setBytesMessageId(new byte[16]);
            consumeLog.setClientIp(new byte[16]);
            consumeLog.setConsumeTime(SystemClock.now());
            consumeLog.setApp(appArr[i % 3]);
            ByteBuffer buffer = ArchiveSerializer.write(consumeLog);
            archiveMappedFileRepository.append(buffer);
        }

        File file = new File(testPath);
        Assert.assertEquals(true, file.isDirectory());

        File[] files = file.listFiles();
        Assert.assertTrue(files.length > 0);

        Thread.sleep(100);
    }

    //@Test
    public void multiWriteConsumeLogTest() {
        String testPath = getTestPath();
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = consumeService.new ArchiveMappedFileRepository(testPath);
        String[] appArr = {"test", "app_test", "app_app_test"};
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int i1 = 0; i1 < writeRecordNum; i1++) {
                    ConsumeLog consumeLog = new ConsumeLog();
                    consumeLog.setBrokerId(i1);
                    consumeLog.setBytesMessageId(new byte[16]);
                    consumeLog.setClientIp(new byte[16]);
                    consumeLog.setConsumeTime(System.currentTimeMillis());
                    consumeLog.setApp(appArr[i1 % 3]);
                    ByteBuffer buffer = ArchiveSerializer.write(consumeLog);
                    archiveMappedFileRepository.append(buffer);
                }
            }, "write-thread-" + i).start();
        }
    }

    @Test
    public void writeConsumeLog2() throws InterruptedException {
        String testPath = getTestPath();
        delTestFolder(testPath);
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = consumeService.new ArchiveMappedFileRepository(testPath);
        String[] appArr = {"test", "app_test", "app_app_test"};
        for (int i = 0; i < 100; i++) {
            ConsumeLog consumeLog = new ConsumeLog();
            consumeLog.setBrokerId(i);
            consumeLog.setBytesMessageId(new byte[16]);
            consumeLog.setClientIp(new byte[16]);
            consumeLog.setConsumeTime(SystemClock.now());
            consumeLog.setApp(appArr[i % 3]);
            ByteBuffer buffer = ArchiveSerializer.write(consumeLog);
            archiveMappedFileRepository.append(buffer);
        }

        Thread.sleep(100);

        for (int i = 100; i < 200; i++) {
            ConsumeLog consumeLog = new ConsumeLog();
            consumeLog.setBrokerId(i);
            consumeLog.setBytesMessageId(new byte[16]);
            consumeLog.setClientIp(new byte[16]);
            consumeLog.setConsumeTime(SystemClock.now());
            consumeLog.setApp(appArr[i % 3]);
            ByteBuffer buffer = ArchiveSerializer.write(consumeLog);
            archiveMappedFileRepository.append(buffer);
        }

        File file = new File(testPath);
        boolean directory = file.isDirectory();
        if (directory) {
            File[] files = file.listFiles();
            Assert.assertEquals(1, files.length);
        }

        for (int i = 0; i < 200; i++) {
            byte[] bytes = archiveMappedFileRepository.readOne();
            if (bytes.length > 0) {
                ConsumeLog read = ArchiveSerializer.read(ByteBuffer.wrap(bytes));
                Assert.assertEquals(i, read.getBrokerId());
            }
        }
    }

    //@Test
    public void readConsumeLog() throws InterruptedException {
        writeConsumeLog();
        String testPath = getTestPath();
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = consumeService.new ArchiveMappedFileRepository(testPath);

        int brokerid = 0;
        for (int i = 0; i < writeRecordNum; i++) {
            byte[] bytes = archiveMappedFileRepository.readOne();
            if (bytes.length > 0) {
                brokerid = i;
                ConsumeLog read = ArchiveSerializer.read(ByteBuffer.wrap(bytes));
                try {
                    Assert.assertEquals(brokerid, read.getBrokerId());
                } catch (Throwable e) {
                    System.out.println("index: " + i + " brokerid: "+ read.getBrokerId());
                    throw e;
                }
            } else {
                brokerid -= 1;
            }
        }
    }

    /**
     * 删除临时目录
     *
     * @param testPath
     */
    private void delTestFolder(String testPath) {
        File file = new File(testPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            assert files != null;
            if (files.length > 0) {
                Arrays.stream(files).forEach(file1 -> file1.delete());
            }
        }

        file.delete();
    }

    /**
     * 获取一个测试目录
     *
     * @return
     */
    private String getTestPath() {
        String userPath = System.getProperty("java.io.tmpdir");
        String childPath = "/test-folder/";

        return userPath + childPath;
    }

    private String getTestFile() {
        return "";
    }

    //@Test
    public void testConsumeArchiveService() throws NoSuchFieldException, IllegalAccessException {
        ArchiveStore archiveStore = new MockArchiveStore();
        Field archiveField = consumeService.getClass().getDeclaredField("archiveStore");
        archiveField.setAccessible(true);
        archiveField.set(consumeService, archiveStore);

        ConsumeArchiveService.ArchiveMappedFileRepository repository = consumeService.new ArchiveMappedFileRepository(archiveConfig.getArchivePath());
        Field repositoryField = consumeService.getClass().getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(consumeService, repository);


        Field counterField = consumeService.getClass().getDeclaredField("readByteCounter");
        counterField.setAccessible(true);
        counterField.set(consumeService, readByteCounter);

        LoopThread readConsumeLogThread = LoopThread.builder()
                .sleepTime(1, 10)
                .name("ReadAndPutHBase-ConsumeLog-Thread")
                .onException(e -> {
                    System.out.println(e.getMessage());
                    repository.rollBack(readByteCounter.get());
                    System.out.println("finish rollback.");
                })
                .doWork(this::wrapReadAndWrite)
                .build();
        Field readThread = consumeService.getClass().getDeclaredField("readConsumeLogThread");
        readThread.setAccessible(true);
        readThread.set(consumeService, readConsumeLogThread);

        readConsumeLogThread.start();
    }

    private void wrapReadAndWrite() throws InterruptedException, JoyQueueException {
        consumeService.readAndWrite();
    }

    @Test
    public void testArchive() {
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository =
                consumeService.new ArchiveMappedFileRepository(getTestPath());

        try {
            readBatch(archiveMappedFileRepository);
        } catch (Exception e) {
            archiveMappedFileRepository.rollBack(readByteCounter.get());
            try {
                readBatch(archiveMappedFileRepository);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        System.out.println("Finish");
    }

    private void readBatch(ConsumeArchiveService.ArchiveMappedFileRepository repository) throws Exception {
        int readBatchSize;
        int batchSize = 1000;
        int exception = 0;
        do {
            List<ConsumeLog> list = readConsumeLog(repository, batchSize);
            readBatchSize = list.size();
            if (readBatchSize > 0) {
                long startTime = SystemClock.now();

                // 调用存储接口写数据
                System.out.println("Store batch: " + batchSize);
                /*if (++exception == 3) {
                    throw new Exception("Exception for store");
                }*/

                long endTime = SystemClock.now();

            } else {
                break;
            }
        } while (readBatchSize == batchSize);
    }

    private List<ConsumeLog> readConsumeLog(ConsumeArchiveService.ArchiveMappedFileRepository repository, int count) {
        // 每次读取之前清零
        readByteCounter.set(0);

        List<ConsumeLog> list = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            byte[] bytes = repository.readOne();
            // 读到结尾会返回byte[0]
            if (bytes.length > 0) {
                // 1个字节开始符号，4个字节int类型长度信息
                readByteCounter.addAndGet(1 + 4 + bytes.length);
                // 反序列花并放入集合
                list.add(ArchiveSerializer.read(ByteBuffer.wrap(bytes)));
            } else {
                break;
            }
        }
        return list;
    }

    //@Test
    public void testAsyncRead() throws IOException {
        String path = getTestPath();
        String fileName = getTestFile();
        File rFile = new File(path + fileName);
        MappedByteBuffer rMap = new RandomAccessFile(rFile, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 1024);
        rMap.position(100);
        int i100 = rMap.getInt();
        System.out.println(i100);
        print(rMap);
        int iUnknown = rMap.getInt();
        System.out.println(iUnknown);
        print(rMap);
        byte[] bytes = new byte[iUnknown];
        rMap.get(bytes);
        System.out.println(bytes);
        print(rMap);
    }

    //@Test
    public void testMappedFileRollback() throws IOException {
        String path = getTestPath();
        String fileName = getTestFile();
        File file = new File(path + fileName);
        MappedByteBuffer map = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 1024 * 1024 * 16);;
        print(map);
        map.position(6815718);
        boolean find = false;
        do {
            find = rollback(map);

            if (find) {
                print(map);
                int msgLen = map.getInt();
                byte[] bytes = new byte[msgLen];
                map.get(bytes);
                ConsumeLog log = ArchiveSerializer.read(ByteBuffer.wrap(bytes));
                System.out.println(log);
            }
        } while (!find);
    }

    private boolean rollback(MappedByteBuffer map) {
        if (map.get() != Byte.MIN_VALUE) {
            map.position(map.position() - 2);
            return false;
        } else {
            return true;
        }
    }

    //@Test
    public void testMappedFileRW() throws IOException {
        String pathR = getTestPath();
        String fileNameR = getTestFile();
        File fileR = new File(pathR + fileNameR);
        MappedByteBuffer mapR = new RandomAccessFile(fileR, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 1024 * 1024 * 16);;
        print(mapR);
        String pathW = getTestPath();
        String fileNameW = getTestFile();
        File fileW = new File(pathW + fileNameW);
        MappedByteBuffer mapW = new RandomAccessFile(fileW, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 1024 * 1024 * 16);;
        print(mapW);

        boolean isExit = true;
        do {
            if (checkPositionReadable(fileW, fileR, mapR, 5181218)) {
                int msgLen = mapR.getInt();
                byte[] bytes = new byte[msgLen];
                mapR.get(bytes);
                print(mapR);
            }  else if (checkFileEndFlag(mapR)) {
                isExit = false;
                System.out.println("File end, lenght: " + mapR);
                break;
            }
        } while (isExit);
    }

    //@Test
    public void testMappedFileRead() throws IOException {
        AtomicInteger counter = new AtomicInteger(0);
        String path = getTestPath();
        String fileName = getTestFile();
        File file = new File(path + fileName);
        MappedByteBuffer map = new RandomAccessFile(file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 1024 * 1024 * 16);;
        print(map);
        //map.position(5705701);
        map.position(2768394);
        print(map);
        boolean isExit = true;
        int exception = 3;
        int error = 0;
        do {
            counter.set(0);
            for (int i = 0; i < 1000; i++) {
                if (checkStartFlag(map)) {
                    int msgLen = map.getInt();
                    byte[] bytes = new byte[msgLen];
                    map.get(bytes);
                    //print(map);
                    counter.addAndGet(1 + 4 + bytes.length);
                }  else if (checkFileEndFlag(map)) {
                    isExit = false;
                    System.out.println("File end, lenght: " + counter.get());
                    break;
                }
            }
            if (++error % exception == 0) {
                print(map);
                map.position(map.position() - counter.get());
            }
        } while (isExit);
    }

    private boolean checkStartFlag(MappedByteBuffer rMap) {
        if (rMap.position() + 1 < 1024 * 1024 * 16) {
            if (rMap.get() == Byte.MIN_VALUE) {
                return true;
            } else {
                rMap.position(rMap.position() - 1);
            }
        }
        return false;
    }

    private boolean checkFileEndFlag(MappedByteBuffer rMap) {
        if (rMap.position() + 1 <= 1024 * 1024 * 16) {
            if (rMap.get() == Byte.MAX_VALUE) {
                rMap.position(rMap.position() - 1);
                return true;
            } else {
                rMap.position(rMap.position() - 1);
            }
        }
        return false;
    }

    private boolean checkPositionReadable(File rwFile, File rFile, MappedByteBuffer rMap, int position) {
        if (rwFile.getName().equals(rFile.getName())) {
            if (rMap.position() <= position) {
                return checkStartFlag(rMap);
            } else {
                return false;
            }
        }
        return checkStartFlag(rMap);
    }

    private static void print(ByteBuffer buffer) {
        System.out.printf("position: %d, limit: %d, capacity: %d\n",
                buffer.position(), buffer.limit(), buffer.capacity());
    }

}