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
package org.joyqueue.model.query;

import org.joyqueue.model.QKeyword;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class QConfig extends QKeyword {
    private Integer status;
    private String group;
    private String key;

    public QConfig() {
    }

    public QConfig(String group) {
        this.group = group;
    }

    public QConfig(String group, String key) {
        this.group = group;
        this.key = key;
    }

    public QConfig(Integer status, String group, String key) {
        this.status = status;
        this.group = group;
        this.key = key;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
