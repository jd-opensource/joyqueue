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
package org.joyqueue.nsr.ignite.dao;

import com.google.inject.Inject;
import org.joyqueue.nsr.ignite.model.IgniteMessage;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.joyqueue.nsr.ignite.model.IgniteBaseModel.SCHEMA;
import static org.joyqueue.nsr.ignite.model.IgniteMessage.COLUMN_CONTENT;
import static org.joyqueue.nsr.ignite.model.IgniteMessage.COLUMN_TYPE;
import static org.joyqueue.nsr.ignite.model.IgniteMessage.COLUMN_MESSAGE_ID;

public class IgniteMessageDao extends IgniteDao {
    public static CacheConfiguration<String, IgniteMessage> cacheCfg;
    public static final String CACHE_NAME = "notify_message";

    static {
        cacheCfg = new CacheConfiguration<>();
        cacheCfg.setName(CACHE_NAME);
        cacheCfg.setSqlSchema(SCHEMA);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        QueryEntity queryEntity = new QueryEntity();
        queryEntity.setKeyType(Integer.class.getName());
        queryEntity.setValueType(IgniteMessage.class.getName());

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put(COLUMN_MESSAGE_ID, Integer.class.getName());
        fields.put(COLUMN_TYPE, String.class.getName());
        fields.put(COLUMN_CONTENT, String.class.getName());

        queryEntity.setFields(fields);
        queryEntity.setTableName(CACHE_NAME);
        queryEntity.setIndexes(Arrays.asList(new QueryIndex(COLUMN_MESSAGE_ID)));
        cacheCfg.setQueryEntities(Arrays.asList(queryEntity));
    }

    @Inject
    public IgniteMessageDao(Ignite ignite) {
        super(ignite,cacheCfg);
    }
}
