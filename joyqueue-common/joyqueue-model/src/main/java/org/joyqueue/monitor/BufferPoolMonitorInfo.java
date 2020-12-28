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
    private long plUsedBytes;
    private String used;
    private long usedBytes;
    private String maxMemorySize;
    private long maxMemorySizeBytes;
    private String mmpUsed;
    private long mmpUsedBytes;
    private int mmpFd;
    private String directUsed;
    private int directFd;
    private long directUsedBytes;
    private int directDestroy;
    private int directAllocate;
    private int mmapDestroy;
    private int mmapAllocate;

    private List<PLMonitorInfo> plMonitorInfos;

    public String getPlUsed() {
        return plUsed;
    }

    public void setPlUsed(String plUsed) {
        this.plUsed = plUsed;
    }

    public long getPlUsedBytes() {
        return plUsedBytes;
    }

    public void setPlUsedBytes(long plUsedBytes) {
        this.plUsedBytes = plUsedBytes;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public String getMaxMemorySize() {
        return maxMemorySize;
    }

    public void setMaxMemorySize(String maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

    public long getMaxMemorySizeBytes() {
        return maxMemorySizeBytes;
    }

    public void setMaxMemorySizeBytes(long maxMemorySizeBytes) {
        this.maxMemorySizeBytes = maxMemorySizeBytes;
    }

    public String getMmpUsed() {
        return mmpUsed;
    }

    public void setMmpUsed(String mmpUsed) {
        this.mmpUsed = mmpUsed;
    }

    public long getMmpUsedBytes() {
        return mmpUsedBytes;
    }

    public void setMmpUsedBytes(long mmpUsedBytes) {
        this.mmpUsedBytes = mmpUsedBytes;
    }

    public int getMmpFd() {
        return mmpFd;
    }

    public void setMmpFd(int mmpFd) {
        this.mmpFd = mmpFd;
    }

    public String getDirectUsed() {
        return directUsed;
    }

    public void setDirectUsed(String directUsed) {
        this.directUsed = directUsed;
    }

    public long getDirectUsedBytes() {
        return directUsedBytes;
    }

    public void setDirectUsedBytes(long directUsedBytes) {
        this.directUsedBytes = directUsedBytes;
    }

    public int getDirectFd() {
        return directFd;
    }

    public void setDirectFd(int directFd) {
        this.directFd = directFd;
    }

    public List<PLMonitorInfo> getPlMonitorInfos() {
        return plMonitorInfos;
    }

    public void setPlMonitorInfos(List<PLMonitorInfo> plMonitorInfos) {
        this.plMonitorInfos = plMonitorInfos;
    }

    public int getDirectDestroy() {
        return directDestroy;
    }

    public void setDirectDestroy(int directDestroy) {
        this.directDestroy = directDestroy;
    }

    public int getDirectAllocate() {
        return directAllocate;
    }

    public void setDirectAllocate(int directAllocate) {
        this.directAllocate = directAllocate;
    }

    public int getMmapDestroy() {
        return mmapDestroy;
    }

    public void setMmapDestroy(int mmapDestroy) {
        this.mmapDestroy = mmapDestroy;
    }

    public int getMmapAllocate() {
        return mmapAllocate;
    }

    public void setMmapAllocate(int mmapAllocate) {
        this.mmapAllocate = mmapAllocate;
    }

    public static class PLMonitorInfo {
        private String cached;
        private String usedPreLoad;
        private String totalSize;
        private String bufferSize;
        private long cachedBytes;
        private long usedPreLoadBytes;
        private long totalSizeBytes;
        private long bufferSizeBytes;

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

        public long getCachedBytes() {
            return cachedBytes;
        }

        public void setCachedBytes(long cachedBytes) {
            this.cachedBytes = cachedBytes;
        }

        public long getUsedPreLoadBytes() {
            return usedPreLoadBytes;
        }

        public void setUsedPreLoadBytes(long usedPreLoadBytes) {
            this.usedPreLoadBytes = usedPreLoadBytes;
        }

        public long getTotalSizeBytes() {
            return totalSizeBytes;
        }

        public void setTotalSizeBytes(long totalSizeBytes) {
            this.totalSizeBytes = totalSizeBytes;
        }

        public long getBufferSizeBytes() {
            return bufferSizeBytes;
        }

        public void setBufferSizeBytes(long bufferSizeBytes) {
            this.bufferSizeBytes = bufferSizeBytes;
        }
    }
}

