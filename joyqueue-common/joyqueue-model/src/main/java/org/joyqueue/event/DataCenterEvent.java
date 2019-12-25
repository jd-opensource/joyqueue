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
package org.joyqueue.event;

@Deprecated
public class DataCenterEvent extends MetaEvent {
    /**
     * 区域
     */
    private String region;
    /**
     * code
     */
    private String code;
    /**
     * url
     */
    private String url;

    public DataCenterEvent() {
    }

    public DataCenterEvent(EventType eventType, String region, String code, String url) {
        super(eventType);
        this.region = region;
        this.code = code;
        this.url = url;
    }
    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }
   public static DataCenterEvent add(String region, String code, String url) {
        return new DataCenterEvent(EventType.ADD_DATACENTER, region, code, url);
    }

    public static DataCenterEvent remove(String region, String code, String url) {
        return new DataCenterEvent(EventType.REMOVE_DATACENTER, region, code, url);
    }


    public static DataCenterEvent update(String region, String code, String url) {
        return new DataCenterEvent(EventType.UPDATE_DATACENTER, region, code, url);
    }


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
