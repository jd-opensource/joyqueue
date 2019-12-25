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
package org.joyqueue.monitor;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/5/29.
 */
public class BufferPoolMonitorInfo {
    private String plUsed;
    private String used;
    private String maxMemorySize;
    private String mmpUsed;
    private String directUsed;

    private List<PLMonitorInfo> plMonitorInfos;

    public String getPlUsed() {
        return plUsed;
    }

    public void setPlUsed(String plUsed) {
        this.plUsed = plUsed;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getMaxMemorySize() {
        return maxMemorySize;
    }

    public void setMaxMemorySize(String maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

    public String getMmpUsed() {
        return mmpUsed;
    }

    public void setMmpUsed(String mmpUsed) {
        this.mmpUsed = mmpUsed;
    }

    public String getDirectUsed() {
        return directUsed;
    }

    public void setDirectUsed(String directUsed) {
        this.directUsed = directUsed;
    }

    public List<PLMonitorInfo> getPlMonitorInfos() {
        return plMonitorInfos;
    }

    public void setPlMonitorInfos(List<PLMonitorInfo> plMonitorInfos) {
        this.plMonitorInfos = plMonitorInfos;
    }

    public static class PLMonitorInfo {
        private String cached;
        private String usedPreLoad;
        private String totalSize;
        private String bufferSize;

        public String getCached() {
            return cached;
        }

        public void setCached(String cached) {
            this.cached = cached;
        }

        public String getUsedPreLoad() {
            return usedPreLoad;
        }

        public void setUsedPreLoad(String usedPreLoad) {
            this.usedPreLoad = usedPreLoad;
        }

        public String getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(String totalSize) {
            this.totalSize = totalSize;
        }

        public String getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(String bufferSize) {
            this.bufferSize = bufferSize;
        }
    }
}

