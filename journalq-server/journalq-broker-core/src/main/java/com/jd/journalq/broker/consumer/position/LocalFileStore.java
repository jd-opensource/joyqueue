package com.jd.journalq.broker.consumer.position;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jd.journalq.broker.consumer.position.model.ConsumeBill;
import com.jd.journalq.broker.consumer.model.ConsumePartition;
import com.jd.journalq.broker.consumer.position.model.Position;
import com.jd.journalq.common.network.session.Joint;
import com.jd.journalq.toolkit.concurrent.LoopThread;
import com.jd.journalq.toolkit.io.Files;
import com.jd.journalq.toolkit.lang.Charsets;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.lang.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于本地文件存储的消费位点快照
 * <p>
 * Created by chengzhiliang on 2019/2/27.
 */
public class LocalFileStore implements PositionStore<ConsumePartition, Position> {

    private Logger logger = LoggerFactory.getLogger(LocalFileStore.class);

    // 位置信号量
    private final Object[] fileUpdateLock = new Object[0];
    // 消费位置配置文件
    PositionConfig config;
    // 消费消息序号文件
    private File indexFile;
    // 消费消息序号文件备份(双写)
    private File indexFileBack;
    // 快照文件目录
    private String basePath;
    // 消费者消费序号
    private ConcurrentMap<ConsumePartition, Position> consumePositionCache = new ConcurrentHashMap<>();
    // 检查点线程
    private LoopThread thread;

    // 是否启动
    private AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * 设置的文件快照目录
     *
     * @param basePath
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void start() throws Exception {
        if (isStarted.get()) {
            logger.info("LocalFileStore is started, can not be restart.");
            return;
        }
        Preconditions.checkArgument(StringUtils.isNotEmpty(basePath), "basePath can not be null!");

        this.config = new PositionConfig(basePath);
        this.indexFile = this.config.getPositionFile();
        this.indexFileBack = new File(this.indexFile.getParentFile(), this.indexFile.getName() + PositionConfig.BACK_SUFFIX);

        Files.createFile(indexFile);
        Files.createFile(indexFileBack);

        initConsumePositionCache();

        this.thread = LoopThread.builder()
                .sleepTime(1000 * 30, 1000 * 30)
                .name("Consume-Position-Store-Thread")
                .onException(e -> logger.error(e.getMessage(), e))
                .doWork(this::doFlush)
                .build();

        this.thread.start();

        isStarted.set(true);

        logger.info("LocalFileStore is started.");
    }

    /**
     * 初始化消费位置缓存
     */
    private void initConsumePositionCache() throws Exception {
        ConcurrentMap<ConsumePartition, Position> recoverCache = recover();
        this.consumePositionCache = recoverCache;
    }

    @Override
    public void stop() {
        if (thread != null) {
            thread.stop();
        }
        isStarted.set(false);

        logger.info("LocalFileStore is stop.");
    }

    @Override
    public boolean isStarted() {
        return isStarted.get();
    }

    @Override
    public Position get(ConsumePartition key) {
        return consumePositionCache.get(key);
    }

    @Override
    public void put(ConsumePartition key, Position value) {
        consumePositionCache.put(key, value);
    }

    @Override
    public Position remove(ConsumePartition key) {
        return consumePositionCache.remove(key);
    }

    @Override
    public Position putIfAbsent(ConsumePartition key, Position value) {
        return consumePositionCache.putIfAbsent(key, value);
    }

    @Override
    public void forceFlush() {
        doFlush();
    }

    @Override
    public Iterator<ConsumePartition> iterator() {
        return consumePositionCache.keySet().iterator();
    }

    /**
     * 刷新偏移量到磁盘
     */
    protected void doFlush() {
        Map<Joint, List<ConsumeBill>> jointListMap = cloneIndexCache(consumePositionCache);
        dump(jointListMap);
    }

    /**
     * 以线程安全的方式clone一份深拷贝
     *
     * @return
     * @throws CloneNotSupportedException
     */
    private Map<Joint, List<ConsumeBill>> cloneIndexCache(ConcurrentMap<ConsumePartition, Position> consumePositionCache) {
        Map<Joint, List<ConsumeBill>> copyOfConsumeBills = new HashMap<>(consumePositionCache.size());
        for (Map.Entry<ConsumePartition, Position> entry : consumePositionCache.entrySet()) {
            ConsumePartition consumePartition = entry.getKey();
            String topic = consumePartition.getTopic();
            String app = consumePartition.getApp();
            short partition = consumePartition.getPartition();
            int partitionGroup = consumePartition.getPartitionGroup();
            Position position = entry.getValue();
            Joint joint = new Joint(topic, app);
            List<ConsumeBill> consumeBills = copyOfConsumeBills.get(joint);
            if (consumeBills == null) {
                consumeBills = new ArrayList<>();
                copyOfConsumeBills.put(joint, consumeBills);
            }
            ConsumeBill consumeBill = new ConsumeBill(partitionGroup, partition, position);
            consumeBills.add(consumeBill);
        }
        return copyOfConsumeBills;
    }

    /**
     * 持久化快照
     *
     */
    private void dump(Map<Joint, List<ConsumeBill>> jointListMap) {
        try {
            String jsonStr = JSON.toJSONString(jointListMap);
            synchronized (fileUpdateLock) {
                writeFile(indexFile, jsonStr);
                writeFile(indexFileBack, jsonStr);
            }
        } catch (Exception e) {
            logger.error("flush index error.", e);
        }
    }

    /**
     * 恢复快照
     *
     * @return
     * @throws IOException
     */
    public ConcurrentMap<ConsumePartition, Position> recover() throws IOException {
        ConcurrentMap<ConsumePartition, Position> consumePositionCache = new ConcurrentHashMap<>();

        Map<Joint, List<ConsumeBill>> consumeBills;
        try {
            consumeBills = loadFromFile(indexFile, new TypeReference<Map<Joint, List<ConsumeBill>>>() {
            });
        } catch (Exception e) {
            consumeBills = loadFromFile(indexFileBack, new TypeReference<Map<Joint, List<ConsumeBill>>>() {
            });
        }

        if (consumeBills != null) {
            consumeBills.entrySet().stream().forEach(entry -> {
                Joint key = entry.getKey();
                entry.getValue().stream().forEach(val ->
                        consumePositionCache.putIfAbsent(new ConsumePartition(key.getTopic(), key.getApp(), val.getPartition()), new Position(val.getAckStartIndex(), val.getAckCurIndex(), val.getPullStartIndex(), val.getPullCurIndex()))
                );
            });
        }

        return consumePositionCache;
    }


    /**
     * 输出JSON到文件
     *
     * @param file    文件
     * @param content 内容
     * @throws IOException
     */
    private void writeFile(File file, String content) throws IOException {
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(content);
            writer.flush();
        } finally {
            Close.close(writer);
        }
    }

    /**
     * 从文件读取数据
     *
     * @param file          文件
     * @param typeReference 对象引用
     * @param <T>           泛型
     * @return 数据对象
     * @throws IOException
     */
    private <T> T loadFromFile(File file, TypeReference<T> typeReference) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            if (builder.length() > 0) {
                return JSON.parseObject(builder.toString(), typeReference);
            } else {
                return null;
            }
        } finally {
            Close.close(reader);
        }
    }


}
