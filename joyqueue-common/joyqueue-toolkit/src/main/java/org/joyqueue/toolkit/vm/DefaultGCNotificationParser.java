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

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GC notification parser
 * reference http://www.fasterj.com/articles/gcnotifs.shtml
 *
 **/
public class DefaultGCNotificationParser implements NotificationListener {
    // gc event listeners
    private List<GCEventListener> gcListeners=new ArrayList();
    public void addListener(GCEventListener listener){
        this.gcListeners.add(listener);
    }
    @Override
    public void handleNotification(Notification notification, Object handback) {
//        System.out.println(notification);
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                GCEvent event=new GCEvent();
                        event.setGcInfo(info);
                        event.setType(GCEventType.typeOf(info.getGcAction()));
                        event.setMemorySections(new HashMap<>());
                Map<String, MemoryUsage> beforeGc = info.getGcInfo().getMemoryUsageBeforeGc();
                Map<String, MemoryUsage> afterGc = info.getGcInfo().getMemoryUsageAfterGc();
                for (Map.Entry<String, MemoryUsage> entry : afterGc.entrySet()) {
                    String name = entry.getKey();
                    MemoryUsage after = entry.getValue();
                    MemoryUsage before = beforeGc.get(name);
                    JVMMemorySectionInfo memoryInfo=new JVMMemorySectionInfo();
                    memoryInfo.setName(name);
                    memoryInfo.setAfter(after);
                    memoryInfo.setBefore(before);
                    event.getMemorySections().put(name,memoryInfo);
                }
                for(GCEventListener listener:gcListeners){
                    listener.handleNotification(event);
                }
        }else{
            System.out.println(notification.getType() +" ignore");
        }
    }
}
