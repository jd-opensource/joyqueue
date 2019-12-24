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

import org.joyqueue.broker.archive.ArchiveSerializer;
import org.joyqueue.broker.archive.ConsumeArchiveService;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.toolkit.time.SystemClock;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by chengzhiliang on 2018/12/19.
 */
public class ConsumeArchiveServiceTest {

    final int writeRecordNum = 1000000;

    @Test
    public void writeConsumeLog() throws InterruptedException {
        String testPath = getTestPath();
        delTestFolder(testPath);
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = new ConsumeArchiveService.ArchiveMappedFileRepository(testPath);
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

    @Test
    public void writeConsumeLog2() throws InterruptedException {
        String testPath = getTestPath();
        delTestFolder(testPath);
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = new ConsumeArchiveService.ArchiveMappedFileRepository(testPath);
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

    @Test
    public void readConsumeLog() throws InterruptedException {
        writeConsumeLog();
        String testPath = getTestPath();
        ConsumeArchiveService.ArchiveMappedFileRepository archiveMappedFileRepository = new ConsumeArchiveService.ArchiveMappedFileRepository(testPath);

        for (int i = 0; i < writeRecordNum; i++) {
            byte[] bytes = archiveMappedFileRepository.readOne();
            if (bytes.length > 0) {
                ConsumeLog read = ArchiveSerializer.read(ByteBuffer.wrap(bytes));
                Assert.assertEquals(i, read.getBrokerId());
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

}