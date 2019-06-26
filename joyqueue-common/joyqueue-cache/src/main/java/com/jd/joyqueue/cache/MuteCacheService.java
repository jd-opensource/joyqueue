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
package com.jd.joyqueue.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 不抛出异常的缓存服务
 */
public class MuteCacheService implements com.jd.joyqueue.cache.CacheService {

    private static Logger logger = LoggerFactory.getLogger(MuteCacheService.class);

    // 委托的缓存服务
    private com.jd.joyqueue.cache.CacheService delegate;

    public MuteCacheService(com.jd.joyqueue.cache.CacheService delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate can not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void rpush(final String key, final String value, final int maxCount) {
        try {
            delegate.rpush(key, value, maxCount);
        } catch (Throwable e) {
            logger.error("rpush error." + e.getMessage());
        }
    }

    @Override
    public void rpush(final byte[] key, final byte[] value, final int maxCount) {
        try {
            delegate.rpush(key, value, maxCount);
        } catch (Throwable e) {
            logger.error("rpush error." + e.getMessage());
        }
    }

    @Override
    public List<String> range(final String key, final int from, final int to) {
        try {
            return delegate.range(key, from, to);
        } catch (Throwable e) {
            logger.error("range {} {} {} error.{}", key, from, to, e.getMessage());
        }
        return null;
    }

    @Override
    public List<byte[]> range(final byte[] key, final int from, final int to) {
        try {
            return delegate.range(key, from, to);
        } catch (Throwable e) {
            logger.error("range error." + e.getMessage());
        }
        return null;
    }

    @Override
    public void put(final String key, final String value) {
        try {
            delegate.put(key, value);
        } catch (Throwable e) {
            logger.error("put {} {} error.{}", key, value, e.getMessage());
        }
    }

    @Override
    public String get(final String key) {
        try {
            return delegate.get(key);
        } catch (Throwable e) {
            logger.error("get {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public byte[] get(byte[] key) {
        try {
            return delegate.get(key);
        } catch (Throwable e) {
            logger.error("get {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long incr(final String key) {
        try {
            return delegate.incr(key);
        } catch (Throwable e) {
            logger.error("incr {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long incrBy(final String key, final long count) {
        try {
            return delegate.incrBy(key, count);
        } catch (Throwable e) {
            logger.error("incrBy {} {} error.{}", key, count, e.getMessage());
        }
        return null;
    }

    @Override
    public Long decr(final String key) {
        try {
            return delegate.decr(key);
        } catch (Throwable e) {
            logger.error("decr {} error. {}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long decrBy(final String key, final long count) {
        try {
            return delegate.decrBy(key, count);
        } catch (Throwable e) {
            logger.error("decrBy {} {} error.{}", key, count, e.getMessage());
        }
        return null;
    }

    @Override
    public void delete(final String key) {
        try {
            delegate.delete(key);
        } catch (Throwable e) {
            logger.error("delete {} error.{}", key, e.getMessage());
        }
    }

    @Override
    public void delete(final byte[] key) {
        try {
            delegate.delete(key);
        } catch (Throwable e) {
            logger.error("delete {} error.{}", key, e.getMessage());
        }
    }

    @Override
    public void set(String key, String value) {
        try {
            delegate.set(key, value);
        } catch (Throwable e) {
            logger.error("set {} error.{}", key, e.getMessage());
        }
    }

    @Override
    public void setex(byte[] key, int seconds, byte[] value) {
        try {
            delegate.setex(key, seconds, value);
        } catch (Throwable e) {
            logger.error("setex {} error.{}", key, e.getMessage());
        }
    }

    @Override
    public void setex(String key, int seconds, String value) {
        try {
            delegate.setex(key, seconds, value);
        } catch (Throwable e) {
            logger.error("setex {} error.{}", key, e.getMessage());
        }
    }

    @Override
    public void zadd(String key, double score, String member) {
        try {
            delegate.zadd(key, score, member);
        } catch (Throwable e) {
            logger.error("zadd {} error.{}", key, e.getMessage());
        }
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        try {
            return delegate.zrange(key, start, end);
        } catch (Throwable e) {
            logger.error("zrange {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        try {
            return delegate.zrangeByScore(key, min, max);
        } catch (Throwable e) {
            logger.error("zrangeByScore {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, long offset, long count) {
        try {
            return delegate.zrangeByScore(key, min, max, offset, count);
        } catch (Throwable e) {
            logger.error("zrangeByScore {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long zcount(String key, double min, double max) {
        try {
            return delegate.zcount(key, min, max);
        } catch (Throwable e) {
            logger.error("zcount {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long zcard(String key) {
        try {
            return delegate.zcard(key);
        } catch (Throwable e) {
            logger.error("zcard {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long zrem(String key, String... member) {
        try {
            return delegate.zrem(key, member);
        } catch (Throwable e) {
            logger.error("zrem {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Double zscore(String key, String member) {
        try {
            return delegate.zscore(key, member);
        } catch (Throwable e) {
            logger.error("zscore {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Boolean setnx(String key, String value) {
        try {
            return delegate.setnx(key, value);
        } catch (Throwable e) {
            logger.error("setnx {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Boolean expire(String key, int seconds) {
        try {
            return delegate.expire(key, seconds);
        } catch (Throwable e) {
            logger.error("expire {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long ttl(String key) {
        try {
            return delegate.ttl(key);
        } catch (Throwable e) {
            logger.error("ttl {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public String lpop(String key) {
        try {
            return delegate.lpop(key);
        } catch (Throwable e) {
            logger.error("lpop {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long rpush(String key, String... value) {
        try {
            return delegate.rpush(key, value);
        } catch (Throwable e) {
            logger.error("rpush {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long llen(String key) {
        try {
            return delegate.llen(key);
        } catch (Throwable e) {
            logger.error("llen {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long lrem(String key, long count, String value) {
        try {
            return delegate.lrem(key, count, value);
        } catch (Throwable e) {
            logger.error("lrem {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public String spop(String key) {
        try {
            return delegate.spop(key);
        } catch (Throwable e) {
            logger.error("spop {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long sadd(String key, String... values) {
        try {
            return delegate.sadd(key, values);
        } catch (Throwable e) {
            logger.error("sadd {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long scard(String key) {
        try {
            return delegate.scard(key);
        } catch (Throwable e) {
            logger.error("scard {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public Long srem(String key, String... values) {
        try {
            return delegate.srem(key, values);
        } catch (Throwable e) {
            logger.error("srem {} error.{}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public com.jd.joyqueue.cache.CacheService getDelegate() {
        return delegate;
    }

    @Override
    public boolean hSet(String key, String field, String value) {
        return delegate.hSet(key, field, value);
    }

    @Override
    public String hGet(String key, String field) {
        return delegate.hGet(key, field);
    }

    @Override
    public Long hDel(String key, String... fields) {
        return delegate.hDel(key, fields);
    }

    @Override
    public Map hGetAll(String key) {
        return delegate.hGetAll(key);
    }
}
