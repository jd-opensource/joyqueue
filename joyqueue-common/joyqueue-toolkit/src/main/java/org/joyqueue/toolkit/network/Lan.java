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
package org.joyqueue.toolkit.network;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 局域网,多个网段由逗号或分号隔开
 * 单个网段格式如下：
 * <ul>
 * <li>172.168.1.0/24</li>
 * <li>172.168.1.0-172.168.1.255</li>
 * <li>172.168.1.1</li>
 * <li>172.168.1.*</li>
 * </ul>
 */
public class Lan {
    // 多个网段
    private List<Segment> segments = new ArrayList<Segment>();
    // ID
    private int id;
    // 名称
    private String name;

    public Lan(String ips) {
        this(0, null, ips);
    }

    public Lan(int id, String name, String ips) {
        this.id = id;
        this.name = name;
        if (ips != null && !ips.isEmpty()) {
            StringTokenizer tokenizer = new StringTokenizer(ips, ",;", false);
            String segment;
            while (tokenizer.hasMoreTokens()) {
                segment = tokenizer.nextToken();
                if (segment != null && !segment.isEmpty()) {
                    segments.add(new Segment(segment));
                }
            }
        }
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * 是否包含指定IP
     *
     * @param ip IP
     * @return 布尔值
     */
    public boolean contains(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        if (segments != null) {
            for (Segment segment : segments) {
                if (segment.contains(ip)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Lan lan = (Lan) o;

        if (id != lan.id) {
            return false;
        }
        if (segments != null ? !segments.equals(lan.segments) : lan.segments != null) {
            return false;
        }
        return name != null ? name.equals(lan.name) : lan.name == null;

    }

    @Override
    public int hashCode() {
        int result = segments != null ? segments.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(100);
        int count = 0;
        for (Segment segment : segments) {
            if (count++ > 0) {
                builder.append(';');
            }
            builder.append(segment.toString());
        }
        return builder.toString();
    }
}