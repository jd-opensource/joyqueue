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
package org.joyqueue.toolkit.vm;

import com.sun.management.GarbageCollectionNotificationInfo;

import java.util.Map;

public class GCEvent {
    private GCEventType type;
    private GarbageCollectionNotificationInfo gcInfo;
    private Map<String,JVMMemorySectionInfo> memorySections;

    public GCEventType getType() {
        return type;
    }

    public void setType(GCEventType type) {
        this.type = type;
    }

    public GarbageCollectionNotificationInfo getGcInfo() {
        return gcInfo;
    }

    public void setGcInfo(GarbageCollectionNotificationInfo gcInfo) {
        this.gcInfo = gcInfo;
    }

    public Map<String, JVMMemorySectionInfo> getMemorySections() {
        return memorySections;
    }

    public void setMemorySections(Map<String, JVMMemorySectionInfo> memorySections) {
        this.memorySections = memorySections;
    }
}
