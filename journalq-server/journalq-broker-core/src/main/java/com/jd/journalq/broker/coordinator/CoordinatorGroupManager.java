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
package com.jd.journalq.broker.coordinator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * CoordinatorGroupManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorGroupManager {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorGroupManager.class);

    private String namespace;
    private CoordinatorConfig config;

    private Cache<String, CoordinatorGroup> groupCache;

    public CoordinatorGroupManager(String namespace, CoordinatorConfig config) {
        this.namespace = namespace;
        this.config = config;
        this.groupCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getGroupExpireTime(), TimeUnit.MILLISECONDS)
                .build();
    }

    public <T extends CoordinatorGroup> T getGroup(String groupId) {
        return (T) groupCache.getIfPresent(groupId);
    }

    public <T extends CoordinatorGroup> List<T> getGroups() {
        return (List<T>) Lists.newArrayList(groupCache.asMap().values());
    }

    public <T extends CoordinatorGroup> T getOrCreateGroup(CoordinatorGroup group) {
        return getOrCreateGroup(group.getId(), new Callable<CoordinatorGroup>() {
            @Override
            public CoordinatorGroup call() throws Exception {
                return group;
            }
        });
    }

    public <T extends CoordinatorGroup> T getOrCreateGroup(String groupId) {
        return getOrCreateGroup(groupId, new Callable<CoordinatorGroup>() {
            @Override
            public CoordinatorGroup call() throws Exception {
                return new CoordinatorGroup(groupId);
            }
        });
    }

    public <T extends CoordinatorGroup> T getOrCreateGroup(String groupId, Callable<CoordinatorGroup> callable) {
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