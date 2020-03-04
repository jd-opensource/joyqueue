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

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.joyqueue.broker.Plugins;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.network.session.Connection;
import org.joyqueue.server.archive.store.api.ArchiveStore;
import org.joyqueue.server.archive.store.model.ConsumeLog;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.lang.Close;
import org.joyqueue.toolkit.security.Md5;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 消费归档服务
 * <p>
 * Created by chengzhiliang on 2018/12/4.
 */
public class ConsumeArchiveService extends Service {
    private static final Logger logger = LoggerFactory.getLogger(ConsumeArchiveService.class);

    // 消费归档存日志文件
    private ArchiveMappedFileRepository repository;
    // 归档存储服务
    private ArchiveStore archiveStore;
    // 集群管理
    private ClusterManager clusterManager;
    // 归档日志
    private ArchiveConfig archiveConfig;

    // 统计当前读取的字节数，用于异常回滚
    private AtomicInteger readByteCounter;

    // 负责读取本地消费日志文件
    private LoopThread readConsumeLogThread;

    // 负责删除已经归档本地文件
    private LoopThread cleanConsumeLogFileThread;

    private PointTracer tracer;

    public ConsumeArchiveService(ArchiveConfig archiveConfig, ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.archiveConfig = archiveConfig;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (archiveStore == null) {
            archiveStore = Plugins.ARCHIVESTORE.get();
        }
        archiveStore.setNameSpace(archiveConfig.getNamespace());
        logger.info("Get archive store namespace [{}] by archive config.", archiveConfig.getNamespace());

        Preconditions.checkArgument(archiveStore != null, "archive store can not be null.");

        this.repository = new ArchiveMappedFileRepository(archiveConfig.getArchivePath());
        this.readByteCounter = new AtomicInteger(0);

        this.tracer = Plugins.TRACERERVICE.get(archiveConfig.getTracerType());
        this.readConsumeLogThread = LoopThread.builder()
                .sleepTime(1, 10)
                .name("ReadAndPutHBase-ConsumeLog-Thread")
                .onException(e -> {
                    logger.error(e.getMessage(), e);
                    repository.rollBack(readByteCounter.get());
                    logger.info("finish rollback.");
                })
                .doWork(this::readAndWrite)
                .build();

        this.cleanConsumeLogFileThread = LoopThread.builder()
                .sleepTime(1000 * 10, 1000 * 10)
                .name("CleanArchiveFile-ConsumeLog-Thread")
                .onException(e -> logger.error(e.getMessage(), e))
                .doWork(this::cleanAndRollWriteFile)
                .build();
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        archiveStore.start();
        readConsumeLogThread.start();
        cleanConsumeLogFileThread.start();
    }


    @Override
    protected void doStop() {
        super.doStop();
        Close.close(readConsumeLogThread);
        Close.close(cleanConsumeLogFileThread);
        Close.close(repository);
        Close.close(archiveStore);
    }

    /**
     * 读本地文件写归档存储服务
     */
    private void readAndWrite() throws JoyQueueException, InterruptedException {
        // 读信息，一次读指定条数
        int readBatchSize;
        int batchSize=archiveConfig.getConsumeBatchNum();
        do {
            List<ConsumeLog> list = readConsumeLog(batchSize);
            readBatchSize=list.size();
            if (readBatchSize > 0) {
                long startTime = SystemClock.now();

                // 调用存储接口写数据
                archiveStore.putConsumeLog(list, tracer);

                long endTime = SystemClock.now();

                logger.debug("Write consumeLogs size:{} to archive store, and elapsed time {}ms", list.size(), endTime - startTime);

                int consumeWriteDelay = archiveConfig.getConsumeWriteDelay();
                if (consumeWriteDelay > 0) {
                    Thread.sleep(consumeWriteDelay);
                }

            } else {
                if (repository.rFile != null && repository.rMap != null) {
                    logger.debug("read file name {}, read position {}", repository.rFile.getName(), repository.rMap.toString());
                } else {
                    logger.debug("read file is null.");
                }
                break;
            }
        }while(readBatchSize==batchSize);
    }

    private void cleanAndRollWriteFile() {
        // 删除已归档文件
        repository.delArchivedFile();
        // 5分钟滚动生成一个新的写文件，旧文件可归档
        repository.tryFinishCurWriteFile();
    }

    /**
     * 指定条数读取本地文件
     *
     * @param count 指定读取的条数
     * @return K-V结构的消费日志
     */
    private List<ConsumeLog> readConsumeLog(int count) {
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

    /**
     * 获取剩余未归档消息日志大小
     *
     * @return
     */
    public long getRemainConsumeLogFileNum() {
        if (!archiveConfig.isReamingEnable()) {
            return 0;
        }
        int localFileNum = repository.getLocalFileNum();
        return (long) localFileNum * repository.getPageSize();
    }

    /**
     * 将消费日志记录到本地文件（消费成功ACK之后调用）
     *
     * @param connection 客户端连接信息
     * @param locations  应答位置信息数组
     */
    public void appendConsumeLog(Connection connection, MessageLocation[] locations) throws JoyQueueException {
        if (!isStarted()) {
            // 没有启动消费归档服务，添加消费日志
            logger.debug("ConsumeArchiveService not be started.");
            return;
        }
        List<ConsumeLog> logList = convert(connection, locations);
        logList.stream().forEach(log -> {
            // 序列化
            ByteBuffer buffer = ArchiveSerializer.write(log);
            appendLog(buffer);
            ArchiveSerializer.release(buffer);
        });
    }

    /**
     * 将连接信息和应答位置信息转换成消费日志
     *
     * @param connection 客户端连接信息
     * @param locations  应答位置信息数组
     * @return
     */
    private List<ConsumeLog> convert(Connection connection, MessageLocation[] locations) throws JoyQueueException {
        List<ConsumeLog> list = new LinkedList<>();
        for (MessageLocation location : locations) {
            ConsumeLog log = new ConsumeLog();

            byte[] bytesMsgId = buildMessageId(location);
            log.setBytesMessageId(bytesMsgId);

            log.setApp(connection.getApp());
            log.setBrokerId(clusterManager.getBrokerId());
            log.setClientIp(connection.getAddress());
            log.setConsumeTime(SystemClock.now());

            list.add(log);
        }
        return list;
    }

    /**
     * 构造消息Id
     *
     * @param location 应答位置信息
     * @return
     */
    private byte[] buildMessageId(MessageLocation location) {
        String messageId = location.getTopic() + location.getPartition() + location.getIndex();
        byte[] messageIdBytes = new byte[0];
        try {
            messageIdBytes = Md5.INSTANCE.encrypt(messageId.getBytes(), null);
        } catch (GeneralSecurityException e) {
            logger.error("topic:{}, partition:{}, index:{}, exception:{}", location.getTopic(), location.getPartition(), location.getIndex(), e);
        }
        return messageIdBytes;
    }

    /**
     * 追加缓存信息到本地消费日志文件
     *
     * @param buffer
     */
    private synchronized void appendLog(ByteBuffer buffer) {
        repository.append(buffer);
    }

    /**
     * 本地日志日志文件存储
     */
    static class ArchiveMappedFileRepository implements Closeable {
        // 消费归档文件本地根存储路径
        private String baseDir;

        // 写文件
        private File rwFile;
        // 写文件的Mapped
        private MappedByteBuffer rwMap;
        // 随机读文件
        private RandomAccessFile rwRaf;
        // 读写文件通道
        private FileChannel rwFileChannel;


        // 读文件
        private File rFile;
        // 读文件的Mapped
        private MappedByteBuffer rMap;
        // 随机读文件
        private RandomAccessFile rRaf;
        // 读写文件通道
        private FileChannel rFileChannel;


        // 归档消息的位置
        private volatile long position = 0;
        // 单个归档文件的大小
        private final long pageSize = 1024 * 1024 * 16; // 16M

        ArchiveMappedFileRepository(String baseDir) {
            this.baseDir = baseDir;
            recover();
        }

        /**
         * 恢复未写完的文件
         */
        private void recover() {
            File lastFile = LastFile();
            if (lastFile == null) {
                return;
            }
            rwFile = lastFile;
            try {
                rwRaf = new RandomAccessFile(rwFile, "rw");
                rwFileChannel = rwRaf.getChannel();
                rwMap = rwFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, rwFile.length());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            // 恢复位置
            while (checkStartFlag(rwMap)) {
                int len = rwMap.getInt();
                rwMap.get(new byte[len]);
            }
            // 如果已经写满，则关闭流，等待下一次调用的时候初始化
            if (checkFileEndFlag(rwMap)) {
                close();
                // 如果最后一个文件写满了，则直接返回，下次调用append的时候新建一个文件写
                return;
            }
            // 设置当前文件继续写入的位置
            position = rwMap.position();
        }

        /**
         * 追加日志到本地文件
         *
         * @param buffer
         */
        public synchronized void append(ByteBuffer buffer) {
            position += buffer.limit();
            // 首次创建文件
            if (rwMap == null) {
                newMappedRWFile();
                // may notify reader
                position = 0;
                append(buffer);
            } else if (1 + position >= pageSize) {
                // 一个文件结束时（1个字节记录开始记录 + 记录长度） 小于 文件长度
                rwMap.put(Byte.MAX_VALUE);
                rwMap = null;
                append(buffer);
            } else {
                // buffer 为空时直接返回
                if (buffer.limit() == 0) {
                    logger.debug("append buffer limit is zero.");
                    return;
                }
                // 先写一个开始标记
                rwMap.put(Byte.MIN_VALUE);
                // 记录一个标示位占用长度
                position += 1;
                // 写入记录内容
                rwMap.put(buffer);
            }
        }

        /**
         * 创建并映射一个读写文件
         */
        public void newMappedRWFile() {
            try {
                if (rwFileChannel != null) rwFileChannel.close();
                rwFileChannel = null;
                if (rwRaf != null) rwRaf.close();
                rwRaf = null;
                rwFile = null;

                File parent = new File(baseDir);
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                rwFile = FileUtils.getFile(baseDir, SystemClock.now() + "");

                rwRaf = new RandomAccessFile(rwFile, "rw");
                rwFileChannel = rwRaf.getChannel();
                rwMap = rwFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, pageSize);
            } catch (Exception ex) {
                logger.error("create and mapped file error.", ex);
            }
        }


        /**
         * 映射一个只读文件
         */
        private void mappedReadOnlyFile() {
            try {
                closeCurrentReadFile();
                rRaf = new RandomAccessFile(rFile, "r");
                rFileChannel = rRaf.getChannel();
                rMap = rFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, pageSize);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        /**
         * 读取一条消费日志
         *
         * @return 消费日志
         */
        public byte[] readOne() {
            if (rMap == null) {
                if (nextFile() != null) {
                    mappedReadOnlyFile();
                } else {
                    return new byte[0];
                }
            }
            // 检查一条消费日志开始标记
            if (checkStartFlag(rMap)) {
                int msgLen = rMap.getInt();
                byte[] bytes = new byte[msgLen];
                rMap.get(bytes);

                return bytes;
            } else if (checkFileEndFlag(rMap)) {
                logger.debug("Finish reading the file {}.{}", rFile, rMap.toString());

                // 换文件
                if (nextFile() != null) {
                    // 映射新文件
                    mappedReadOnlyFile();
                    // 继续读取消息
                    return readOne();
                }
            }
            return new byte[0];
        }

        /**
         * 检查一条消息开始的标记
         *
         * @return
         */
        private boolean checkStartFlag(MappedByteBuffer rMap) {
            if (rMap.position() + 1 < pageSize) {
                if (rMap.get() == Byte.MIN_VALUE) {
                    return true;
                } else {
                    rMap.position(rMap.position() - 1);
                }
            }
            return false;
        }

        /**
         * 检查是否到文件结束
         *
         * @return
         */
        private boolean checkFileEndFlag(MappedByteBuffer rMap) {
            if (rMap.position() + 1 <= pageSize) {
                if (rMap.get() == Byte.MAX_VALUE) {
                    rMap.position(rMap.position() - 1);
                    return true;
                } else {
                    rMap.position(rMap.position() - 1);
                }
            }
            return false;
        }

        /**
         * 重置buffer位置
         *
         * @param interval
         */
        private void rollBack(int interval) {
            if (rMap != null) {
                int position = rMap.position();
                int newPosition = position - interval;
                rMap.position(newPosition);
            }
        }


        /**
         * 关闭文件管道
         */
        @Override
        public void close() {
            try {
                closeCurrentReadFile();
                closeCurrentWriteFile();
            } catch (IOException e) {
                logger.error("delete read file error.", e);
            }
        }

        /**
         * Close current read file
         **/
        public void closeCurrentReadFile() throws IOException {
            if (rFileChannel != null) {
                rFileChannel.close();
            }
            if (rRaf != null) {
                rRaf.close();
            }
        }

        /**
         *
         * Close current write file
         *
         **/

        public void closeCurrentWriteFile() throws IOException {
            if (rwFileChannel != null) {
                rwFileChannel.close();
            }
            if (rwRaf != null) {
                rwRaf.close();
            }
        }


        /**
         * 找下个被读的文件
         *
         * @return 只读文件，返回null代表当前没有可读文件
         */
        private File nextFile() {
            File file = new File(baseDir);
            String[] list = file.list();
            if (list == null || list.length == 1) {
                logger.debug("only one write file.");
                // 归档文件目录下的没有文件，或者文件数等于1，则表示没有可归档文件，返回null
                return null;
            }

            logger.debug("archive file list {}", Arrays.toString(list));

            final String concurrentFileName = rFile == null ? "" : rFile.getName();
            List<String> sorted = Arrays.asList(list).stream().filter(name -> name.compareTo(concurrentFileName) > 0)
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());

            if (sorted.size() > 1) {
                // 未归档文件数大于1，获取第1个未归档文件
                String fileName = sorted.get(0);
                File tempFile = new File(baseDir + fileName);
                rFile = tempFile;
                logger.debug("current read consume event file {}",tempFile);
                return rFile;
            } else {
                logger.debug("only one write file.");
            }

            return null;

        }

        /**
         * 找到最新的文件
         *
         * @return
         */
        private File LastFile() {
            File file = new File(baseDir);
            String[] list = file.list();
            if (list == null) {
                return null;
            }
            Optional<String> first = Arrays.asList(list).stream().sorted(Comparator.reverseOrder()).findFirst();
            if (first.isPresent()) {
                String fileName = first.get();
                File tempFile = new File(baseDir + fileName);
                rwFile = tempFile;
                return rwFile;
            }
            return null;
        }

        /**
         * 删除已归档的文件
         */
        private void delArchivedFile() {
            // 遍历删除已归档文件
            List<String> archivedFileList = getArchivedFileList();
            if (CollectionUtils.isNotEmpty(archivedFileList)) {
                archivedFileList.stream().forEach(fileName -> {
                    new File(baseDir + fileName).delete();
                });
            }
        }

        /**
         * 获取已经归档的文件
         *
         * @return
         */
        private List<String> getArchivedFileList() {
            // 没有调用到readOne方法，不会初始化rFile，防止空指针，加一下判断
            if (rFile == null) {
                logger.debug("Can not get archive file list cause by consume archive read file have no init.");
                return null;
            }

            File file = new File(baseDir);
            String[] list = file.list();
            if (list == null) {
                return null;
            }
            // 返回已完成归档的文件集合
            return Arrays.asList(list).stream().filter(name -> name.compareTo(rFile.getName()) < 0).collect(Collectors.toList());
        }

        /**
         * 获取当前的文件数量
         *
         * @return
         */
        public int getLocalFileNum() {
            File file = new File(baseDir);
            String[] list = file.list();
            if (list == null){
                return 0;
            }
            return list.length;
        }

        /**
         * 当前读到文件的位置
         *
         * @return
         */
        public long getReadPosition() {
            return rMap.position();
        }

        /**
         * 当前写到文件的位置
         *
         * @return
         */
        public long getWritePosition() {
            return rwMap.position();
        }

        /**
         * 文件大小
         *
         * @return
         */
        public long getPageSize() {
            return pageSize;
        }

        /**
         * 结束当前正在写的文件
         */
        public synchronized void tryFinishCurWriteFile() {
            if (rwFile == null) {
                return;
            }
            // 5分钟滚动生成一个新的写文件
            String name = rwFile.getName();
            long now = SystemClock.now();
            // position > 0 说明有归档记录写入文件，并且5分钟没有写满
            if (position > 0 && now - Long.parseLong(name) >= 1000 * 60 * 1) {
                // 直接将当前写文件的位置设置为文件大小，下次一次append的时候会新建一个文件继续写
                position = pageSize;
                append(ByteBuffer.wrap(new byte[0]));

                logger.info("reset write file {} position {} to pageSize.", rwFile.getName(), rwMap.toString());
            }
        }
    }

}
