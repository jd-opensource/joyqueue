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

package org.joyqueue.broker.monitor.metrics;
import com.alibaba.fastjson.annotation.JSONField;
import com.codahale.metrics.*;

/**
 *
 * LoadMetric with interval snapshot
 * interval concurrency and recent 1m, 5m and 15m load
 * tps, interval tp75,95,98,etc.
 *
 **/

public class LoadMetric implements Metered, Sampling {
    private Meter meter;
    private Histogram histogram;

    public LoadMetric(){
        reset();
    }
    public LoadMetric(Meter meter, Histogram histogram){
        this.meter=meter;
        this.histogram=histogram;
    }

    /**
     * Load metric snapshot
     *
     **/
    @JSONField(serialize = false)
    public LoadMetric getIntervalLoadMetric(){
       LoadMetric snapshot= new LoadMetric(meter,histogram);
                  reset();
       return snapshot;
    }

    /**
     *  Record event frequencies
     * @param count  event frequencies
     *
     **/
    public void mark(long count) {
        this.meter.mark(count);
    }

    /**
     *  Record event frequencies and elapsed time
     *  @param time   take time
     *  @param count  event frequencies
     *
     **/
    public void mark(double time, long count) {
        this.meter.mark(count);
        this.histogram.update((long) time);
    }

    /**
     *
     *  Reset meter and histogram
     **/
    private void reset(){
        this.meter=new Meter();
        this.histogram = new Histogram(new ExponentiallyDecayingReservoir());
    }


    @Override
    public long getCount() {
        return meter.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    /**
     *  Throughput per second
     *
     **/
    @Override
    public double getMeanRate() {
        return meter.getMeanRate();
    }


    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }

    @Override
    public Snapshot getSnapshot() {
        return histogram.getSnapshot();
    }
}

