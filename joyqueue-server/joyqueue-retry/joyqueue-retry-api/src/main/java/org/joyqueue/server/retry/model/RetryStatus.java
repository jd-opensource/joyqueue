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
package org.joyqueue.server.retry.model;

/**
 * Created by chengzhiliang on 2019/2/20.
 */
public enum RetryStatus {

    RETRY_ING((short)1,"重试中"),
    RETRY_EXPIRE((short)-2,"过期"),
    RETRY_DELETE((short)-1,"删除"),
    RETRY_SUCCESS((short)0,"成功");

    private short value;
    private String name;

    RetryStatus(short value, String name) {
        this.value = value;
        this.name = name;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
