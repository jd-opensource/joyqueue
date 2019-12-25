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

import java.util.Objects;

/**
 * @author wylixiaobin
 * Date: 2018/8/30
 */
public class DataCenter {
    private static final String UNKNOWN = "UNKNOWN";
    public static DataCenter DEFAULT = new DataCenter(UNKNOWN, UNKNOWN);
    /**
     * 数据中心code
     */
    protected String code;

    /**
     * 数据中心名称
     */
    protected String name;
    /**
     * 数据中心所在区域
     */
    protected String region;

    /**
     * 数据中心匹配规则
     */
    private String url;

    public String getId() {
        return getRegion() + "_" + getCode();
    }

    public DataCenter(String code, String region) {
        this.code = code;
        this.region = region;

    }

    public DataCenter(String code, String name, String region, String url) {
        this.code = code;
        this.region = region;
        this.name = name;
        this.url = url;
    }

    public DataCenter() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof DataCenter)) return false;
        DataCenter that = (DataCenter) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(name, that.name) &&
                Objects.equals(region, that.region) &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, name, region, url);
    }
}
