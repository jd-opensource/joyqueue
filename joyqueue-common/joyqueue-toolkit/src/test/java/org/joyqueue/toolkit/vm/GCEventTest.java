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
import com.sun.management.GcInfo;
import org.joyqueue.toolkit.vm.DefaultGCNotificationParser;
import org.joyqueue.toolkit.vm.GCEvent;
import org.joyqueue.toolkit.vm.GCEventListener;
import org.joyqueue.toolkit.vm.GarbageCollectorMonitor;
import org.joyqueue.toolkit.vm.JVMMemorySectionInfo;
import org.joyqueue.toolkit.vm.MemoryStat;
import org.junit.Test;
import sun.misc.SharedSecrets;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/GcInfo.html
 *
 **/
public class GCEventTest {

    @Test
    public void gcEvent(){
        StringBuilder builder=new StringBuilder();
        GCEventListener gcEventListener=(GCEvent t)->{
            GarbageCollectionNotificationInfo notificationInfo=t.getGcInfo();
            GcInfo gcInfo=notificationInfo.getGcInfo();

            builder.append(t.getType().name()+
                    ";gc cause: "+ notificationInfo.getGcCause()+
                    ";gc action: "+ notificationInfo.getGcAction()+
                    ";gc duration: "+gcInfo.getDuration()+" ms"+
                    ";gc end time: "+gcInfo.getEndTime()+" ms\n");
            for(Map.Entry<String, JVMMemorySectionInfo> e:t.getMemorySections().entrySet()){
               builder.append("key ").append(e.getKey()).append(",");
               builder.append("gc max ").append(String.format("%d->%d",e.getValue().getBefore().getMax(),e.getValue().getAfter().getMax())).append(" bytes,")
                       .append("used ").append(String.format("%d->%d",e.getValue().getBefore().getUsed(),e.getValue().getAfter().getUsed())).append(" bytes").append("\n");

            }
            builder.append("\n");
            System.out.println(builder.toString());
            builder.setLength(0);
        };
        DefaultGCNotificationParser notifyListener=new DefaultGCNotificationParser();
        notifyListener.addListener(gcEventListener);
        GarbageCollectorMonitor gcMonitor=new GarbageCollectorMonitor();
        gcMonitor.addGCEventListener(notifyListener);
        ScheduledExecutorService executorService= Executors.newSingleThreadScheduledExecutor();
        Map<Integer, byte[]> kv=new HashMap<>();
        executorService.scheduleAtFixedRate(()->{
//          byte[] bytes;
          for(int i=1;i<50000;i++){
              kv.put(i,new byte[i*1024]);
          }
          kv.clear();
        },0,1000, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(10 * 1000);
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }
        executorService.shutdown();

    }

    @Test
    public void memory(){
        GarbageCollectorMonitor gcMonitor=new GarbageCollectorMonitor();
        MemoryStat stat=gcMonitor.memSnapshot();
        StringBuilder builder=new StringBuilder();
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(2*1024*1024);

        long direct=SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();

        builder.append("heap:").append("init ").append(stat.getHeapInit()).append(" bytes,")
                .append("used ").append(stat.getHeapUsed()).append(" bytes,")

                .append("committed ").append(stat.getHeapCommitted()).append(" bytes,")
                .append("max ").append(stat.getHeapMax()).append(" bytes\n");

        builder.append("non-heap:").append("init ").append(stat.getNonHeapInit()).append(" bytes,")
                .append("used ").append(stat.getNonHeapUsed()).append(" bytes,")
                .append("committed ").append(stat.getNonHeapCommitted()).append(" bytes,")

                .append("max ").append(stat.getNonHeapMax()).append(" bytes\n");
        System.out.println(builder.toString());
        System.out.println("direct:"+direct);
    }

}
