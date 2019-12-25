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
package org.joyqueue.broker.consumer.model;

import org.joyqueue.network.session.Consumer;

/**
 * Created by lining48 on 2018/8/16.
 */
public class OwnerShip {
    // 所有者
    private String owner;
    // 占用过期事件
    private volatile long expireTime;
    // 创建时间
    private long createTime;

    /**
     * 根据消费者信息构造OwnerShip对象
     *
     * @param consumer 消费者信息
     */
    public OwnerShip(Consumer consumer) {
       this(consumer.getId(), 0);
    }

    public OwnerShip(String owner, long expireTime) {
        this.owner = owner;
        this.expireTime = expireTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isExpire(long now) {
        return expireTime > 0 && expireTime <= now;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        OwnerShip ownerShip = (OwnerShip)obj;
        return this.owner.equals(ownerShip.getOwner());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OwnerShip{");
        sb.append("owner='").append(owner).append('\'');
        sb.append(", create=").append(createTime);
        sb.append(", expireTime=").append(expireTime);
        sb.append(", times:").append(expireTime - createTime);
        sb.append('}');
        return sb.toString();
    }
}
