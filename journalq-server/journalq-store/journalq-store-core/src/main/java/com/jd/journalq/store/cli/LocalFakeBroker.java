package com.jd.journalq.store.cli;

import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.store.*;
import com.jd.journalq.store.message.BatchMessageParser;
import com.jd.journalq.store.utils.MessageUtils;
import com.jd.journalq.toolkit.concurrent.LoopThread;
import com.jd.journalq.toolkit.metric.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 本地模拟broker，用于测试存储性能
 * @author liyue25
 * Date: 2018/9/10
 */
public class LocalFakeBroker {
    private static final Logger logger = LoggerFactory.getLogger(LocalFakeBroker.class);

    private final static String TOPIC_PREFIX = "local_broker_";
    private final static long K = 1024,M = K * K;
    private final static long G = K * M, T = K * G;
    private final static Map<String,Long> UNIT_MAP = new HashMap<>(4);
    static {
        UNIT_MAP.put("k",K);
        UNIT_MAP.put("m",M);
        UNIT_MAP.put("g",G);
        UNIT_MAP.put("t",T);
    }
    // 批量发送数量
    private int batchSize = 1024;
    // 每条消息大小
    private int bodySize = 1024;

    // topic 数量
    private int topicCount = 1;

    // 每个topic的 Partition Group 数量
    private int groupCount = 1;

    // 每个partition group 的 partition 数量

    private int partitionCount = 1;

    // 每个 partition group 的consumer数量
    private int consumerCount = 1;
    private int producerCount = 1;
    private long metricIntervalMs = 0L;
    private String path = getBasePath();

    private Store store;

    private StoreConfig storeConfig = new StoreConfig(null);

    private List<LoopThread> loopThreads = new ArrayList<>();

    private Metric input, output;

    // 开启批消息时，每批消息的消息数量
    private short batchMessageSize = 1;

    private String getBasePath() {
        String systemTempPath = System.getProperty("java.io.tmpdir");
        if(!systemTempPath.endsWith(File.separator)) systemTempPath += File.separator;
        return systemTempPath + "local_fake_broker";
    }

    public void loadConfig(String filename) {
        Properties prop = new Properties();

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename)){
            // load a properties file
            prop.load(input);

            batchSize = (int) parseSize(prop.getProperty("batchSize"), batchSize);
            bodySize = (int) parseSize(prop.getProperty("bodySize"), bodySize);
            topicCount = Integer.parseInt(prop.getProperty("topicCount",String.valueOf(topicCount)));
            groupCount = Integer.parseInt(prop.getProperty("groupCount",String.valueOf(groupCount)));
            partitionCount = Integer.parseInt(prop.getProperty("partitionCount",String.valueOf(partitionCount)));
            consumerCount = Integer.parseInt(prop.getProperty("consumerCount",String.valueOf(consumerCount)));
            producerCount = Integer.parseInt(prop.getProperty("producerCount",String.valueOf(producerCount)));
            batchMessageSize = Short.parseShort(prop.getProperty("batchMessageSize", String.valueOf(batchMessageSize)));
            this.metricIntervalMs = Long.parseLong(prop.getProperty("printMetricIntervalMs", String.valueOf(storeConfig.getPrintMetricIntervalMs())));
            path = prop.getProperty("path",path);


            storeConfig.setMessageFileSize((int)parseSize(prop.getProperty("messageFileSize"),storeConfig.getMessageFileSize()));
            storeConfig.setIndexFileSize((int)parseSize(prop.getProperty("indexFileSize"),storeConfig.getIndexFileSize()));
            storeConfig.setMaxMessageLength((int)parseSize(prop.getProperty("maxMessageLength"),storeConfig.getMaxMessageLength()));
            storeConfig.setWriteRequestCacheSize((int)parseSize(prop.getProperty("writeRequestCacheSize"),storeConfig.getWriteRequestCacheSize()));
            storeConfig.setFlushIntervalMs(Long.parseLong(prop.getProperty("flushIntervalMs", String.valueOf(storeConfig.getFlushIntervalMs()))));
            storeConfig.setPreLoadBufferCoreCount(Integer.parseInt(prop.getProperty("buffer.coreSize",String.valueOf(storeConfig.getPreLoadBufferCoreCount()))));
            storeConfig.setPreLoadBufferMaxCount(Integer.parseInt(prop.getProperty("buffer.maxSize",String.valueOf(storeConfig.getPreLoadBufferMaxCount()))));
            storeConfig.setMaxDirtySize(parseSize(prop.getProperty("maxDirtySize"), storeConfig.getMaxDirtySize()));
            storeConfig.setPrintMetricIntervalMs(Long.parseLong(prop.getProperty("printMetricIntervalMs", String.valueOf(storeConfig.getPrintMetricIntervalMs()))));
            storeConfig.setPath(path);
        } catch (IOException ex) {
            logger.warn("Exception: ",ex);
        }
    }

    private long parseSize(String sizeString,long defaultValue){
        long size = defaultValue;
        if(sizeString != null ) {
            String trimString = sizeString.trim().toLowerCase();
            if(!trimString.isEmpty()) {
                long unit = UNIT_MAP.getOrDefault(trimString.substring(sizeString.length() - 1),1L);
                if(unit > 1L) {
                    trimString = trimString.substring(0, trimString.length() - 1).trim();
                }
                size = Long.parseLong(trimString) * unit;
            }
        }
        return size;
    }



    public void start() throws Exception {
        loadConfig("config.properties");


        store = new Store(storeConfig);
        store.start();
        if(metricIntervalMs > 0) {
            input = new Metric("Produce", producerCount, new String [] { "total"}, new String[] {"tps"}, new String [] {"traffic"});
            output = new Metric("Consume", consumerCount * partitionCount, new String [] { "total"}, new String[] {"tps"}, new String [] {"traffic"});
        }

        for (int i = 0; i < topicCount; i++) {
            String topic = String.format("%s%03d", TOPIC_PREFIX,i);
            for (int j = 0; j < groupCount; j++) {
                short [] partitions = new short[partitionCount];
                for (int k = 0; k < partitionCount; k++) {
                    partitions[k] = (short) ((j * groupCount + k) % Short.MAX_VALUE);
                }
                if(!store.partitionGroupExists(topic,j)) {
                    store.createPartitionGroup(topic, j, partitions, new int[]{0});
                } else {
                    store.restorePartitionGroup(topic, j);
                }
                store.getReplicableStore(topic,j).enable();

                for (int k = 0; k < producerCount; k++) {
                    Metric.MetricInstance metricInstance = null;
                    if(null != input) {
                        metricInstance = input.getMetricInstances().get(k);
                    }
                    Producer producer = new Producer(store.getStore(topic, j, QosLevel.RECEIVE),partitions[(i * groupCount + j * producerCount + k) % partitions.length],batchSize,bodySize, batchMessageSize, metricInstance);

                    loopThreads.add(LoopThread.builder()
                            .name(String.format("ProducerThread-%s-%d-%d",topic,j,k))
                            .sleepTime(0L,0L)
                            .onException(e->logger.warn("Exception:",e))
                            .doWork(producer::produce)
                            .build());
                }

                for (int k = 0; k < consumerCount; k++) {
                    for (int i1 = 0; i1 < partitions.length; i1++) {
                        Short partition = partitions[i1];
                        Metric.MetricInstance metricInstance = null;
                        if (null != output) {
                            metricInstance = output.getMetricInstances().get(k * partitionCount + i1);
                        }
                        Consumer consumer = new Consumer(store.getStore(topic, j, QosLevel.PERSISTENCE), partition, batchSize, metricInstance);
                        loopThreads.add(LoopThread.builder()
                                .name(String.format("ConsumerThread-%s-%d-%d:%d", topic, j, partition, k))
                                .sleepTime(10L, 100L)
                                .onException(e -> logger.warn("Exception:", e))
                                .doWork(consumer::consume)
                                .build());
                    }
                }
            }
        }

        if(metricIntervalMs > 0) {

            loopThreads.add(LoopThread.builder()
                    .sleepTime(metricIntervalMs, metricIntervalMs)
                    .name("Metric-Thread")
                    .onException(e -> logger.warn("Exception:", e))
                    .doWork(() -> {
                        input.reportAndReset();
                        output.reportAndReset();
                    }).build());

        }
        for(LoopThread loopThread: loopThreads) {
            loopThread.start();
        }


        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

    }



    private void stop() {
        try {
            loopThreads.parallelStream().forEach(t -> {
                System.out.println(String.format("Shutting down loop thread: %s...", t.getName()));
                t.stop();
            });
            System.out.println("Shutting down store...");
            if (null != store ) {
                store.stop();
                store.close();
            }
            System.out.println("Bye!");
        } catch (Exception e) {
            logger.warn("Exception: ", e);
        }
    }


    public static void main(String [] args) throws Exception {
        LocalFakeBroker localFakeBroker = new LocalFakeBroker();
        localFakeBroker.start();
    }



    static class Producer {
        private final PartitionGroupStore store;
        private WriteRequest [] messages;
        private final Metric.MetricInstance metric;
        private long size;
        private final int batchSize,bodySize;
        private final short partition;
        private final short batchMessageSize;
        Producer(PartitionGroupStore store, short partition, int batchSize, int bodySize, short batchMessageSize, Metric.MetricInstance metric) {
            this.store = store;
            this.metric = metric;
            this.partition = partition;
            this.batchSize = batchSize;
            this.bodySize = bodySize;
            this.batchMessageSize = batchMessageSize;
            messages = MessageUtils.build(batchSize,bodySize)
                    .stream()
                    .peek(b -> {
                        if(batchMessageSize > 1) {
                            BatchMessageParser.setBatch(b, true);
                            BatchMessageParser.setBatchSize(b,batchMessageSize);
                        }
                    })
                    .map(b->new WriteRequest(partition,b)).toArray(WriteRequest[]::new);

            size = Arrays.stream(messages).map(WriteRequest::getBuffer).mapToLong(ByteBuffer::remaining).sum();

        }

        private void produce() throws Exception{
            WriteRequest [] messages = Arrays.stream(this.messages)
                    .peek(m -> m.setBuffer(MessageUtils.build1024()))
                    .peek(m -> {
                        if(batchMessageSize > 1) {
                            BatchMessageParser.setBatch(m.getBuffer(), true);
                            BatchMessageParser.setBatchSize(m.getBuffer(),batchMessageSize);
                        }
                    }).toArray(WriteRequest[]::new);
            long t0 = System.nanoTime();
//            syncWrite( messages, t0);
//            asyncWrite( messages, t0);
            asyncWriteNoResponse(messages, t0);

        }

        private void asyncWrite(WriteRequest[] messages, long t0) {
            store.asyncWrite(writeResult-> {
                if(null != writeResult && JMQCode.SUCCESS == writeResult.getCode()) {
                    if(null != metric) {
                        long t1 = System.nanoTime();
                        metric.addCounter("tps", messages.length);
                        metric.addTraffic("traffic",size);
                        metric.addLatency("total", t1 - t0);
                    }
                }else {
                    logger.warn("Write Failed!");
                }
            }, messages);
        }

        private void asyncWriteNoResponse(WriteRequest[] messages, long t0) {
            store.asyncWrite(writeResult-> {
                if (null == writeResult || JMQCode.SUCCESS != writeResult.getCode()) {
                    logger.warn("Write Failed!");
                }
            }, messages);


            if(null != metric) {
                long t1 = System.nanoTime();
                metric.addCounter("tps", messages.length);
                metric.addTraffic("traffic",size);
                metric.addLatency("total", t1 - t0);
            }
        }

        private void syncWrite(WriteRequest [] messages, long t0) throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {

            Future<WriteResult> future = store.asyncWrite(messages);
            WriteResult writeResult = future.get(1, TimeUnit.SECONDS);
            long t1 = System.nanoTime();


            if(null != writeResult && JMQCode.SUCCESS == writeResult.getCode()) {
                if(null != metric) {
                    metric.addCounter("tps", messages.length);
                    metric.addTraffic("traffic",size);
                    metric.addLatency("total", t1 - t0);
                }
            }else {
                logger.warn("Write Failed!");
            }
        }
    }


    static class Consumer {
        private final PartitionGroupStore store;
        private final Short partition;
        private final int batchSize;
        private long index = -1;
        private final Metric.MetricInstance metric;


        Consumer(PartitionGroupStore store, Short partition, int batchSize, Metric.MetricInstance metric) {
            this.store = store;
            this.partition = partition;
            this.batchSize = batchSize;
            this.metric = metric;
        }

        private void consume() throws IOException {
            if(index < 0) index = store.getRightIndex(partition);
            long left = store.getLeftIndex(partition);
            if(index < left) index = left;
//            logger.info("Reading {}...",index);
            boolean hasMore = true;
            while (hasMore) {
                hasMore = false;
                try {
                    long t0 = System.nanoTime();
                    ReadResult readResult = store.read(partition, index, batchSize, 1024 * 1024 * 1024);

                    if (JMQCode.SUCCESS == readResult.getCode()) {
                        index += Arrays.stream(readResult.getMessages()).mapToInt(buffer -> {
                            if(BatchMessageParser.isBatch(buffer)) {
                                return BatchMessageParser.getBatchSize(buffer);
                            } else {
                                return 1;
                            }
                        }).sum();
                        long input = 0L;
                        hasMore = !readResult.isEop();
                        for (ByteBuffer byteBuffer : readResult.getMessages()) {
                            input += byteBuffer.remaining();
                        }
                        long t1 = System.nanoTime();

                        if(null != metric) {
                            metric.addCounter("tps", readResult.getMessages().length);
                            metric.addTraffic("traffic",input);
                            metric.addLatency("total", t1 - t0);
                        }
                    } else {
                        logger.warn("Read failed!");
                    }
                }catch (PositionOverflowException ignored) {}

            }
        }

    }



}

