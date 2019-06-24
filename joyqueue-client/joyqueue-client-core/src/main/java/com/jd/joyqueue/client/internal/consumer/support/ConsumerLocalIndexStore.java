/**
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
package com.jd.joyqueue.client.internal.consumer.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.jd.joyqueue.client.internal.consumer.domain.LocalIndexData;
import com.jd.joyqueue.toolkit.concurrent.NamedThreadFactory;
import com.jd.joyqueue.toolkit.service.Service;
import com.jd.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ConsumerLocalIndexStore
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/14
 */
public class ConsumerLocalIndexStore extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerLocalIndexStore.class);

    private String persistPath;
    private int persistInterval;

    private File persistFile;
    private volatile boolean isNeedPersist;
    private ScheduledExecutorService persistTimer;
    private Table<String /** app**/, String /** topic **/, Map<Short, LocalIndexData>> indexTable;

    public ConsumerLocalIndexStore(String persistPath, int persistInterval) {
        this.persistPath = persistPath;
        this.persistInterval = persistInterval;
    }

    @Override
    protected void validate() throws Exception {
        persistFile = initPersistPathFile(persistPath);
        indexTable = doRead(persistFile);
        persistTimer = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("journalq-consumer-local-index-persist"));
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
    }

    public LocalIndexData fetchIndex(String topic, String app, short partition) {
        Map<Short, LocalIndexData> partitionMap = indexTable.get(app, topic);
        if (partitionMap == null) {
            return null;
        }
        return partitionMap.get(partition);
    }

    public boolean saveIndex(String topic, String app, short partition, long index) {
        long now = SystemClock.now();
        Map<Short, LocalIndexData> partitionMap = indexTable.get(app, topic);
        if (partitionMap == null) {
            partitionMap = Maps.newHashMap();
            indexTable.put(app, topic, partitionMap);
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
    protected Table<String, String, Map<Short, LocalIndexData>> doRead(File persistFile) throws Exception {
        String json = FileUtils.readFileToString(persistFile);
        Table<String, String, Map<Short, LocalIndexData>> result = HashBasedTable.create();

        if (StringUtils.isBlank(json)) {
            return result;
        }

        JSONObject jsonObject = JSON.parseObject(json);
        for (String app : jsonObject.keySet()) {
            JSONObject appJsonObject = jsonObject.getJSONObject(app);
            for (String topic : appJsonObject.keySet()) {
                JSONObject topicJsonObject = appJsonObject.getJSONObject(topic);

                Map<Short, LocalIndexData> partitions = Maps.newHashMap();
                for (String partition : topicJsonObject.keySet()) {
                    JSONObject partitionJsonObject = topicJsonObject.getJSONObject(partition);
                    long index = partitionJsonObject.getLong("index");
                    long updateTime = partitionJsonObject.getLong("updateTime");
                    long createTime = partitionJsonObject.getLong("createTime");
                    partitions.put(Short.valueOf(partition), new LocalIndexData(index, updateTime, createTime));
                }

                result.put(app, topic, partitions);
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
        String json = JSON.toJSONString(indexTable.rowMap(), SerializerFeature.DisableCircularReferenceDetect);

        try {
            FileUtils.writeStringToFile(persistFile, json);
        } catch (Exception e) {
            logger.error("write local index error, file: {}", persistFile, e);
        }
    }
}