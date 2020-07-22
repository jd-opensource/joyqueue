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
package org.joyqueue.client.internal.consumer.support;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.client.internal.consumer.domain.LocalIndexData;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ConsumerLocalIndexStore
 *
 * author: gaohaoxiang
 * date: 2018/12/14
 */
public class ConsumerLocalIndexStore extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerLocalIndexStore.class);

    private String persistPath;
    private int persistInterval;

    private File persistFile;
    private volatile boolean isNeedPersist;
    private ScheduledExecutorService persistTimer;
    private Map<String /** app**/, Map<String /** topic **/, Map<Short, LocalIndexData>>> indexTable;

    public ConsumerLocalIndexStore(String persistPath, int persistInterval) {
        this.persistPath = persistPath;
        this.persistInterval = persistInterval;
    }

    @Override
    protected void validate() throws Exception {
        persistFile = initPersistPathFile(persistPath);
        indexTable = doRead(persistFile);
        persistTimer = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("joyqueue-consumer-local-index-persist"));
    }

    @Override
    protected void doStart() throws Exception {
        persistTimer.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                persist();
            }
        }, persistInterval, persistInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        if (persistTimer != null) {
            persistTimer.shutdown();
        }
        persist();
    }

    public LocalIndexData fetchIndex(String topic, String app, short partition) {
        Map<String, Map<Short, LocalIndexData>> topicMap = indexTable.get(app);
        if (topicMap == null) {
            return null;
        }
        Map<Short, LocalIndexData> partitionMap = topicMap.get(topic);
        if (partitionMap == null) {
            return null;
        }
        return partitionMap.get(partition);
    }

    public boolean saveIndex(String topic, String app, short partition, long index) {
        long now = SystemClock.now();
        Map<String, Map<Short, LocalIndexData>> topicMap = indexTable.get(app);
        if (topicMap == null) {
            topicMap = Maps.newHashMap();
            indexTable.put(app, topicMap);
        }

        Map<Short, LocalIndexData> partitionMap = topicMap.get(topic);
        if (partitionMap == null) {
            partitionMap = Maps.newHashMap();
            topicMap.put(topic, partitionMap);
        }

        LocalIndexData localIndexData = partitionMap.get(partition);
        if (localIndexData == null) {
            localIndexData = new LocalIndexData();
            localIndexData.setCreateTime(now);
            partitionMap.put(partition, localIndexData);
        }

        localIndexData.setIndex(index);
        localIndexData.setUpdateTime(now);
        isNeedPersist = true;
        return true;
    }

    protected File initPersistPathFile(String path) throws Exception {
        File persistFile = new File(path);
        if (persistFile.isDirectory()) {
            throw new IllegalArgumentException("local index store path is not directory");
        }
        if (!persistFile.exists()) {
            persistFile.getParentFile().mkdirs();
            persistFile.createNewFile();
        }
        return persistFile;
    }

    // TODO 优化
    // TODO 路径拆分
    protected Map<String, Map<String, Map<Short, LocalIndexData>>> doRead(File persistFile) throws Exception {
        String json = FileUtils.readFileToString(persistFile);
        Map<String, Map<String, Map<Short, LocalIndexData>>> result = Maps.newHashMap();

        if (StringUtils.isBlank(json)) {
            return result;
        }

        Gson gson = new GsonBuilder().create();
        Map<String, Map<String, Map<String, Map<String, Object>>>> map = gson.fromJson(json, Map.class);

        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> appEntry : map.entrySet()) {
            String app = appEntry.getKey();

            for (Map.Entry<String, Map<String, Map<String, Object>>> topicEntry : appEntry.getValue().entrySet()) {
                String topic = topicEntry.getKey();
                Map<Short, LocalIndexData> partitions = Maps.newHashMap();

                for (Map.Entry<String, Map<String, Object>> partitionEntry : topicEntry.getValue().entrySet()) {
                    short partition = Short.valueOf(partitionEntry.getKey());
                    Map<String, Object> values = partitionEntry.getValue();

                    long index = Double.valueOf(String.valueOf(values.get("index"))).longValue();
                    long updateTime = Double.valueOf(String.valueOf(values.get("updateTime"))).longValue();
                    long createTime = Double.valueOf(String.valueOf(values.get("createTime"))).longValue();
                    partitions.put(partition, new LocalIndexData(index, updateTime, createTime));
                }

                Map<String, Map<Short, LocalIndexData>> topicMap = result.get(app);
                if (topicMap == null) {
                    topicMap = Maps.newHashMap();
                    result.put(app, topicMap);
                }
                topicMap.put(topic, partitions);
            }
        }
        return result;
    }

    protected void persist() {
        if (!isNeedPersist) {
            return;
        }
        doPersist(persistFile);
        isNeedPersist = false;
    }

    protected void doPersist(File persistFile) {
        String json = new GsonBuilder().create().toJson(indexTable);

        try {
            FileUtils.writeStringToFile(persistFile, json);
        } catch (Exception e) {
            logger.error("write local index error, file: {}", persistFile, e);
        }
    }
}