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
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.Plugins;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.SubscribeRateLimiter;
import org.joyqueue.domain.Subscription;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.monitor.TraceStat;
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
import java.util.*;
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
    private BrokerContext brokerContext;

    // 统计当前读取的字节数，用于异常回滚
    private AtomicInteger readByteCounter;

    // 负责读取本地消费日志文件
    private LoopThread readConsumeLogThread;

    // 负责删除已经归档本地文件
    private LoopThread cleanConsumeLogFileThread;

    private PointTracer tracer;

    // 归档限流器
    private SubscribeRateLimiter rateLimiterManager;

    public ConsumeArchiveService(ArchiveConfig archiveConfig, ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.archiveConfig = archiveConfig;
    }

    public ConsumeArchiveService(ArchiveConfig archiveConfig, BrokerContext brokerContext, SubscribeRateLimiter rateLimiter) {
        this.archiveConfig = archiveConfig;
        this.brokerContext = brokerContext;
        this.clusterManager = brokerContext.getClusterManager();
        this.rateLimiterManager = rateLimiter;
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
                .daemon(true)
                .onException(e -> {
                    logger.error("ReadAndPutHBase-readAndWrite error, happened consume log [{}], error position [{}], error length [{}], exception {}, {}",
                            repository.rFile.getName(), repository.rMap, readByteCounter.get(), e.getMessage(), e);
                    repository.rollBack(readByteCounter.get());
                    logger.info("Consume-archive: finish rollback consume log [{}], rollback position [{}], rollback length [{}].",
                            repository.rFile.getName(), repository.rMap, readByteCounter.get());
                    readByteCounter.set(0);
                })
                .doWork(this::readAndWrite)
                .build();

        this.cleanConsumeLogFileThread = LoopThread.builder()
                .sleepTime(1000, 1000 * 10)
                .name("CleanArchiveFile-ConsumeLog-Thread")
                .daemon(true)
                .onException(e -> logger.error("CleanArchiveFile-cleanAndRollWriteFile error: {}, {}", e.getMessage(), e))
                .doWork(this::cleanAndRollWriteFile)
                .build();
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        archiveStore.start();
        readConsumeLogThread.start();
        cleanConsumeLogFileThread.start();
        logger.info("Consume-archive: service started.");
    }


    @Override
    protected void doStop() {
        super.doStop();
        Close.close(readConsumeLogThread);
        Close.close(cleanConsumeLogFileThread);
        Close.close(repository);
        Close.close(archiveStore);
        logger.info("Consume-archive: service stopped.");
    }

    /**
     * 读本地文件写归档存储服务
     */
    public void readAndWrite() throws JoyQueueException, InterruptedException {
        // 读信息，一次读指定条数
        int readBatchSize;
        int batchSize=archiveConfig.getConsumeBatchNum();
        do {
            List<ConsumeLog> list = readConsumeLog(batchSize);
            readBatchSize=list.size();
            if (readBatchSize > 0) {
                long startTime = SystemClock.now();

                int count = archiveConfig.getStoreFialedRetryCount();
                do {
                    try {
                        // 调用存储接口写数据
                        archiveStore.putConsumeLog(list, tracer);
                        break;
                    } catch (JoyQueueException e) {
                        logger.error(String.format(
                                "Consume-archive: store failed for consume logs, exception size: %s, root cause: %s, cause stack: %s",
                                list.size(), e.getMessage(), e.getCause()), e);
                        if (--count == 0) {
                            throw e;
                        }
                        Thread.sleep(new Random().nextInt(count) * 1000);
                    }
                } while (count > 0);

                long endTime = SystemClock.now();

                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    logger.info("Consume-archive: write consumeLogs size: {} to archive store, and elapsed time {}ms", list.size(), endTime - startTime);
                }

                int consumeWriteDelay = archiveConfig.getConsumeWriteDelay();
                if (consumeWriteDelay > 0) {
                    Thread.sleep(consumeWriteDelay);
                }

            } else {
                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    if (repository.rFile != null && repository.rMap != null) {
                        logger.info("Consume-archive: read consume log file {}, read position {}", repository.rFile.getName(), repository.rMap.toString());
                    } else {
                        logger.info("Consume-archive: read consume log file is null.");
                    }
                }
                break;
            }
        }while(readBatchSize==batchSize);
    }

    private void cleanAndRollWriteFile() {
        // 删除已归档文件
        repository.delArchivedFile();
        // 1天滚动生成一个新的写文件，旧文件可归档
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
        if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
            logger.info("Consume-archive: begin to read consume log batch: {}", count);
        }
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
        if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
            logger.info("Consume-archive: end to read consume log size: {}", list.size());
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
            logger.warn("ConsumeArchiveService not be started.");
            return;
        }
        TraceStat stat = tracer.begin("org.joyqueue.server.archive.consume.appendConsumeLog");
        if (locations != null && locations.length > 0) {
            if (checkRateLimitAvailable(connection, locations)) {
                List<ConsumeLog> logList = convert(connection, locations);
                logList.forEach(log -> {
                    // 序列化
                    ByteBuffer buffer = ArchiveSerializer.write(log);
                    appendLog(buffer);
                    ArchiveSerializer.release(buffer);
                });
            } else {
                TraceStat limitBroker = tracer.begin("archive.consume.rate.limited");
                TraceStat limitTopic = tracer.begin(String.format("archive.consume.rate.limited.%s.%s", locations[0].getTopic(), connection.getApp()));
                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    logger.warn("Consume-archive: trigger rate limited topic: {}, app: {}", locations[0].getTopic(), connection.getApp());
                }
                tracer.end(limitBroker);
                tracer.end(limitTopic);
            }
        }
        tracer.end(stat);
    }

    private boolean checkRateLimitAvailable(Connection connection, MessageLocation[] locations) {
        RateLimiter rateLimiter = rateLimiterManager.getOrCreate(locations[0].getTopic(), connection.getApp(), Subscription.Type.CONSUMPTION);
        if (rateLimiter == null || rateLimiter.tryAcquireTps(locations.length)) {
            return true;
        }
        return false;
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
            logger.error("Consume-archive: build consume log messageId error, topic:{}, partition:{}, index:{}, exception:{}", location.getTopic(), location.getPartition(), location.getIndex(), e);
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
    class ArchiveMappedFileRepository implements Closeable {
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
        private File previousCloseReadFile;
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
            //position += buffer.limit();
            // 首次创建文件
            if (rwMap == null) {
                newMappedRWFile();
                // may notify reader
                position = 0;
                append(buffer);
            } else if ((position + 1 + buffer.limit()) >= pageSize) {
                // 一个文件结束时（1个字节记录开始记录 + 记录长度） 小于 文件长度
                rwMap.put(Byte.MAX_VALUE);
                rwMap = null;
                append(buffer);
            } else {
                // buffer 为空时直接返回
                if (buffer.limit() == 0) {
                    logger.warn("Consume-archive: append buffer limit is zero.");
                    return;
                }
                // 先写一个开始标记
                rwMap.put(Byte.MIN_VALUE);
                // 写入记录内容
                rwMap.put(buffer);
                // 记录一个标示位占用长度
                position += 1 + buffer.limit();
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
                logger.error("Consume-archive: create and mapped file error.", ex);
            }
        }


        /**
         * 映射一个只读文件
         */
        private void mappedReadOnlyFile() {
            try {
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
            if (checkPositionReadable(rMap)) {
                int msgLen = rMap.getInt();
                byte[] bytes = new byte[msgLen];
                rMap.get(bytes);

                return bytes;
            } else if (checkFileEndFlag(rMap)) {
                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    logger.info("Consume-archive: finish read consume log file: {}, position: {}", rFile, rMap.toString());
                }

                try {
                    closeCurrentReadFile();
                } catch (IOException e) {
                    logger.error("Consume-archive: close current consume log archive file: {}, error: {}", rFile, e);
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

        private boolean checkPositionReadable(MappedByteBuffer rMap) {
            if (rwFile == null || !rwFile.exists()) {
                return checkStartFlag(rMap);
            }
            if (rwFile.getName().equals(rFile.getName())) {
                if (rMap.position() < position) {
                    return checkStartFlag(rMap);
                } else {
                    return false;
                }
            }
            return checkStartFlag(rMap);
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
        public void rollBack(int interval) {
            if (rMap != null) {
                int position = rMap.position();
                int newPosition = position - interval;
                if (ConsumeArchiveService.this.archiveConfig.getLogDetail(
                        ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX,
                                ConsumeArchiveService.this.clusterManager.getBrokerId().toString())) {
                    logger.info("Consume-archive: read consume log rollback, position: [{}], offset: [{}], reset position: [{}]",
                            position, interval, newPosition);
                }
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
                logger.error("Consume-archive: close consume log read&write files error: {}", e);
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
            if (rMap != null) {
                rMap = null;
            }
            previousCloseReadFile = rFile;
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
            if (rwMap != null) {
                rwMap = null;
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
            if (list == null) {
                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    logger.info("Consume-archive: find no consume log files");
                }
                return null;
            }

            if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                logger.info("Consume-archive: find consume log file list: {}", Arrays.toString(list));
            }

            final String previousCloseReadFileName = previousCloseReadFile == null ? "" : previousCloseReadFile.getName();
            List<String> sorted = Arrays.stream(list).filter(name -> name.compareTo(previousCloseReadFileName) > 0)
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());

            if (sorted.size() > 0) {
                // 只要有一个比上一次关闭的文件新(正常情况下新生成的) 或者就只有一个文件(该文件可能因为broker重启后重新打开)
                String fileName = sorted.get(0);
                File tempFile = new File(baseDir + fileName);
                rFile = tempFile;
                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    logger.info("Consume-archive: find earliest consume log file: {}",tempFile);
                }
                return rFile;
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
            Optional<String> last = Arrays.stream(list)
                    .map(name -> new File(baseDir + name))
                    .filter(f -> !f.isDirectory())
                    .map(File::getName).max(Comparator.naturalOrder());
            if (last.isPresent()) {
                String fileName = last.get();
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
                archivedFileList.forEach(fileName -> new File(baseDir + fileName).delete());
            }
        }

        /**
         * 获取已经归档的文件
         *
         * @return
         */
        private List<String> getArchivedFileList() {
            File file = new File(baseDir);
            String[] list = file.list();
            if (list == null) {
                return null;
            }

            // 没有调用到readOne方法，不会初始化rFile，防止空指针，加一下判断
            if (rFile == null) {
                if (archiveConfig.getLogDetail(ArchiveConfig.LOG_DETAIL_CONSUME_PREFIX, clusterManager.getBrokerId().toString())) {
                    logger.info("Consume-archive: there is no archive consume log file list cause current broker is not open archive flag.");
                }
                return null;
            }

            // 返回已完成归档的文件集合
            return Arrays.stream(list).filter(name -> name.compareTo(rFile.getName()) < 0).collect(Collectors.toList());
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
            // position > 0 说明有归档记录写入文件，并且1天没有写满
            if (position > 0 && now - Long.parseLong(name) >= archiveConfig.getLogRetainDuration() * 1000 * 60 * 60) {
                // 直接将当前写文件的位置设置为文件大小，下次一次append的时候会新建一个文件继续写
                position = pageSize;
                append(ByteBuffer.wrap(new byte[0]));

                logger.info("Consume-archive: reset write file {} position {} to pageSize.", rwFile.getName(), rwMap.toString());
            }
        }
    }

}
