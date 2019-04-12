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
package com.jd.journalq.broker.coordinator.domain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.jd.journalq.toolkit.time.SystemClock;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * CoordinatorGroup
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorGroup {

    private static final int MAX_EXPIRED_MEMBER = 100;
    private String id;
    private String extension;
    private ConcurrentMap<String, CoordinatorGroupMember> members = Maps.newConcurrentMap();
    private Cache<String, CoordinatorGroupExpiredMember> expiredMembers = CacheBuilder.newBuilder().maximumSize(MAX_EXPIRED_MEMBER).build();
    private Map<String, CoordinatorGroupExpiredMember> expiredMembersMap;

    public CoordinatorGroup() {

    }

    public CoordinatorGroup(String id) {
        this.id = id;
    }

    public void addExpiredMember(CoordinatorGroupMember member) {
        String host = getHost(member.getConnectionHost());
        try {
            CoordinatorGroupExpiredMember expiredMember = expiredMembers.get(host, new Callable<CoordinatorGroupExpiredMember>() {
                @Override
                public CoordinatorGroupExpiredMember call() throws Exception {
                    return new CoordinatorGroupExpiredMember(host);
                }
            });

            expiredMember.setLatestHeartbeat(member.getLatestHeartbeat());
            expiredMember.setExpireTime(SystemClock.now());
            expiredMember.getExpireTimes().incrementAndGet();
        } catch (ExecutionException e) {

        }
    }

    public Map<String, CoordinatorGroupExpiredMember> expiredMembersToMap() {
        return expiredMembers.asMap();
    }

    protected String getHost(String host) {
        if (host.startsWith("/")) {
            host = host.substring(1);
        }
        if (!host.contains(":")) {
            return host;
        }
        return host.split(":")[0];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public ConcurrentMap<String, CoordinatorGroupMember> getMembers() {
        return members;
    }

    public void setMembers(ConcurrentMap<String, CoordinatorGroupMember> members) {
        this.members = members;
    }

    public void setExpiredMembersMap(Map<String, CoordinatorGroupExpiredMember> expiredMembersMap) {
        this.expiredMembersMap = expiredMembersMap;
    }

    public Map<String, CoordinatorGroupExpiredMember> getExpiredMembers() {
        return expiredMembersMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinatorGroup that = (CoordinatorGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CoordinatorGroup{" +
                "id='" + id + '\'' +
                ", members=" + members +
                '}';
    }
}