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
package io.chubao.joyqueue.toolkit.vm;

import io.chubao.joyqueue.toolkit.service.Service;
import sun.misc.SharedSecrets;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;


public class GarbageCollectorMonitor extends Service implements JVMMonitorService {
    /**
     * add listener to gc Garbage collector gc event
     **/
    @Override
    public void addGCEventListener(NotificationListener listener){
        List<GarbageCollectorMXBean> gcbeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            NotificationEmitter emitter = (NotificationEmitter) gcbean;
            emitter.addNotificationListener(listener, null, null);
        }
    }

    /**
     *  JVM memory snapshot
     **/
    @Override
    public MemoryStat memSnapshot() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean() ;
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
        MemoryStat stat=new MemoryStat();
        // heap
        stat.setHeapMax(heap.getMax());
        stat.setHeapInit(heap.getInit());
        stat.setHeapCommited(heap.getCommitted());
        stat.setHeapUsed(heap.getUsed());
        // non-heap
        stat.setNontHeapInit(nonHeap.getInit());
        stat.setNonHeapMax(nonHeap.getMax());
        stat.setNonHeapUsed(nonHeap.getUsed());
        stat.setNonHeapCommited(nonHeap.getCommitted());
        // allocated by ByteBuffer.allocateDirect()
        stat.setDirectBufferSize(SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed());

        return stat;
    }
}
