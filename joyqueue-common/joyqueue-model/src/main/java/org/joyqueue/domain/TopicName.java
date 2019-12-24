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
package org.joyqueue.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Topic名称, 包含了命名空间
 */
public class TopicName {
    public static final String DEFAULT_NAMESPACE = "";
    public static final String TOPIC_SEPARATOR = ".";
    public static final String TOPIC_SEPARATOR_SPLIT = ".";

    private static final int TOPIC_NAME_CACHE_SIZE = 10240;
    private static final ConcurrentMap<String, TopicName> TOPIC_NAME_CACHE = Maps.newConcurrentMap();

    private String code;
    private String namespace;
    private String fullName;

    public TopicName() {
    }

    public TopicName(String code) {
        this(code, DEFAULT_NAMESPACE);
    }

    public TopicName(String code, String namespace) {
        Preconditions.checkArgument(code != null && !code.isEmpty() && !code.contains(TOPIC_SEPARATOR), "invalid topic.");
        Preconditions.checkArgument(namespace != null, "invalid name space.");
        this.code = code;
        this.namespace = namespace;
    }


    public String getFullName() {
        if (fullName == null) {
            if (DEFAULT_NAMESPACE.equals(namespace)) {
                fullName = code;
            } else {
                fullName = namespace + TOPIC_SEPARATOR + code;
            }
        }
        return fullName;
    }

    public static TopicName parse(String code, String namespace) {
        if (StringUtils.isBlank(namespace)) {
            namespace = DEFAULT_NAMESPACE;
        }
        return new TopicName(code, namespace);
    }

    public static TopicName parse(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return null;
        }

        TopicName topicName = TOPIC_NAME_CACHE.get(fullName);
        if (topicName == null) {
            if (TOPIC_NAME_CACHE.size() > TOPIC_NAME_CACHE_SIZE) {
                TOPIC_NAME_CACHE.clear();
            }
            String[] splits = StringUtils.splitByWholeSeparator(fullName, TOPIC_SEPARATOR_SPLIT);
            if (splits.length == 1) {
                topicName = new TopicName(splits[0]);
            } else {
                topicName = new TopicName(splits[1], splits[0]);
            }
            TOPIC_NAME_CACHE.put(fullName, topicName);
        }
        return topicName;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String topic) {
        this.code = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || !(o instanceof TopicName)){
            return false;
        }
        TopicName that = (TopicName) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, namespace);
    }

    @Override
    public String toString() {
        return "TopicName{" +
                "code='" + code + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
