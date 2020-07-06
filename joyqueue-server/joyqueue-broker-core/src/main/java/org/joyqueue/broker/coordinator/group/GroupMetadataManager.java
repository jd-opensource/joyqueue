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
package org.joyqueue.broker.coordinator.group;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.coordinator.config.CoordinatorConfig;
import org.joyqueue.broker.coordinator.group.domain.GroupMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * GroupMetadataManager
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class GroupMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private String namespace;
    private CoordinatorConfig config;

    private Cache<String, GroupMetadata> groupCache;

    public GroupMetadataManager(String namespace, CoordinatorConfig config) {
        this.namespace = namespace;
        this.config = config;
        this.groupCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getGroupExpireTime(), TimeUnit.MILLISECONDS)
                .build();
    }

    public <T extends GroupMetadata> T getGroup(String groupId) {
        if (StringUtils.isBlank(groupId)) {
            return null;
        }
        return (T) groupCache.getIfPresent(groupId);
    }

    public <T extends GroupMetadata> List<T> getGroups() {
        return (List<T>) Lists.newArrayList(groupCache.asMap().values());
    }

    public <T extends GroupMetadata> T getOrCreateGroup(GroupMetadata group) {
        return getOrCreateGroup(group.getId(), new Callable<GroupMetadata>() {
            @Override
            public GroupMetadata call() throws Exception {
                return group;
            }
        });
    }

    public <T extends GroupMetadata> T getOrCreateGroup(String groupId) {
        if (StringUtils.isBlank(groupId)) {
            return null;
        }
        return getOrCreateGroup(groupId, new Callable<GroupMetadata>() {
            @Override
            public GroupMetadata call() throws Exception {
                return new GroupMetadata(groupId);
            }
        });
    }

    public <T extends GroupMetadata> T getOrCreateGroup(String groupId, Callable<GroupMetadata> callable) {
        if (StringUtils.isBlank(groupId)) {
            return null;
        }
        try {
            return (T) groupCache.get(groupId, callable);
        } catch (ExecutionException e) {
            logger.error("getOrCreate coordinatorGroup exception, groupId: {}", groupId, e);
            return (T) groupCache.getIfPresent(groupId);
        }
    }

    public boolean removeGroup(String groupId) {
        groupCache.invalidate(groupId);
        return true;
    }
}