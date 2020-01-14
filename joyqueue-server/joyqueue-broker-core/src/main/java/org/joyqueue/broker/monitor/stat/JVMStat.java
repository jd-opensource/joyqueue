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

package org.joyqueue.broker.monitor.stat;

import com.alibaba.fastjson.annotation.JSONField;
import org.joyqueue.broker.monitor.metrics.LoadMetric;
import org.joyqueue.toolkit.vm.MemoryStat;

import java.util.concurrent.atomic.AtomicLong;

/**
 *  JVM state include total gc times,elapsed time
 *  and snapshot memory stat
 *
 **/
public class JVMStat {

    //总 gc 次数
    private AtomicLong totalGcTimes;

    //总 gc 耗时
    private AtomicLong  totalGcTime;

    // interval eden gc times
    private LoadMetric edenGcTimes;

    // interval old gc times
    private  LoadMetric oldGcTimes;
    // snapshot
    private   MemoryStat memoryStat;

    // 最新的 snapshot
    private transient JVMStat recentSnapshot;


    public JVMStat(){
       this(new AtomicLong(),new AtomicLong(),new LoadMetric(),new LoadMetric(),null);
    }

    public JVMStat(AtomicLong totalGcTimes, AtomicLong totalGcTime, LoadMetric edenGcTimes, LoadMetric oldGcTimes, MemoryStat memoryStat){

        //总 gc 次数
        this.totalGcTimes=totalGcTimes;
        //总 gc 耗时
        this.totalGcTime=totalGcTime;
        this.edenGcTimes=edenGcTimes;
        this.oldGcTimes=oldGcTimes;
        this.memoryStat=memoryStat;
    }


    public AtomicLong getTotalGcTimes() {
        return totalGcTimes;
    }

    public void setTotalGcTimes(AtomicLong totalGcTimes) {
        this.totalGcTimes = totalGcTimes;
    }

    public AtomicLong getTotalGcTime() {
        return totalGcTime;
    }

    public void setTotalGcTime(AtomicLong totalGcTime) {
        this.totalGcTime = totalGcTime;
    }

    public MemoryStat getMemoryStat() {
        return memoryStat;
    }

    public LoadMetric getEdenGcTimes() {
        return edenGcTimes;
    }

    public void setEdenGcTimes(LoadMetric edenGcTimes) {
        this.edenGcTimes = edenGcTimes;
    }

    public LoadMetric getOldGcTimes() {
        return oldGcTimes;
    }

    public void setOldGcTimes(LoadMetric oldGcTimes) {
        this.oldGcTimes = oldGcTimes;
    }

    public void setMemoryStat(MemoryStat memoryStat) {
        this.memoryStat = memoryStat;
    }

    /**
     *
     * Recent interval snapshot
     *
     **/
    @JSONField(serialize =false)
    public JVMStat getRecentSnapshot() {
        if(recentSnapshot==null){
            recentSnapshot=snapshot();
        }
        return recentSnapshot;
    }

    public void setRecentSnapshot(JVMStat recentSnapshot) {
        this.recentSnapshot = recentSnapshot;
    }

    /**
     *
     * Snapshot and reset

     *
     **/
    @JSONField(serialize =false)
    public JVMStat snapshot(){
       recentSnapshot =new JVMStat(null,null,edenGcTimes.getIntervalLoadMetric(),oldGcTimes.getIntervalLoadMetric(),memoryStat);
       return recentSnapshot;
    }
}

