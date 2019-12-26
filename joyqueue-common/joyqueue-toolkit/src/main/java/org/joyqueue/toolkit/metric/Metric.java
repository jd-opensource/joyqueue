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
package org.joyqueue.toolkit.metric;

import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Metric {
    private final List<MetricInstance> metricInstances;
    private final String name;
    private final String[] latencies, counters, traffics;
    private long resetTime = SystemClock.now();
    private static final Logger logger = LoggerFactory.getLogger(Metric.class);

    public Metric(String name, int instanceCount, String[] latencies, String[] counters, String[] traffics) {
        this.name = name;
        metricInstances = new ArrayList<>(instanceCount);
        IntStream.range(0, instanceCount).forEach(i -> metricInstances.add(new MetricInstance(latencies, counters, traffics)));
        this.latencies = latencies;
        this.counters = counters;
        this.traffics = traffics;
    }

    public List<MetricInstance> getMetricInstances() {
        return metricInstances;
    }

    public String getName() {
        return name;
    }


    public void reportAndReset() {
        long reportTime = SystemClock.now();
        int intervalMs = (int) (reportTime - resetTime);
        logger.info(System.lineSeparator() + "{}：{}", name, Stream.of(
                Arrays.stream(counters)
                        .map(name -> {
                            long cps = 0L;
                            if (intervalMs > 0) {
                                cps = metricInstances.stream()
                                        .mapToLong(instance -> instance.getAndResetCounter(name))
                                        .sum() * 1000L / intervalMs;
                            }
                            return String.format("%s: %d /S", name, cps);
                        }),

                Arrays.stream(traffics)
                        .map(name -> {
                            long cps = 0L;
                            if (intervalMs > 0) {
                                cps = metricInstances.stream()
                                        .mapToLong(instance -> instance.getAndResetTraffic(name))
                                        .sum() * 1000L / intervalMs;
                            }
                            return String.format("%s: %s/S", name, Format.formatSize(cps));
                        }),

                Arrays.stream(latencies)
                        .map(name -> {

                            // 算tp99， tp90
                            long[] sorted = metricInstances.stream()
                                    .map(instance -> instance.getAndResetLatencies(name))
                                    .flatMap(List::stream)
                                    .mapToLong(Long::longValue).sorted().toArray();
                            if (sorted.length > 0) {
                                double avg = Arrays.stream(sorted).average().orElse(0D) / 1000000;
                                double tp90 = sorted[(int) (sorted.length * 0.90)] / 1000000D;
                                double tp99 = sorted[(int) (sorted.length * 0.99)] / 1000000D;
                                double max = sorted[sorted.length - 1] / 1000000D;
                                return String.format("%s: %.4f/%.4f/%.4f/%.4f ms", name, avg, tp90, tp99, max);
                            } else {
                                return String.format("%s: 0/0/0/0 ms", name);
                            }
                        }))
                .reduce(Stream::concat).orElseGet(Stream::empty)
                .collect(Collectors.joining(", ")));
        resetTime = reportTime;

    }

    public static class Latency {
        private List<Long> latencies = Collections.synchronizedList(new LinkedList<>());

        void add(long latency) {
            latencies.add(latency);
        }

        List<Long> getAndReset() {
            List<Long> ret = new ArrayList<>(latencies);
            latencies.clear();
            return ret;
        }
    }

    public static class Counter {
        private final AtomicLong atomicLong = new AtomicLong(0L);

        void add(long count) {
            atomicLong.addAndGet(count);
        }

        long getAndReset() {
            return atomicLong.getAndSet(0L);
        }
    }


    public static class Traffic {
        private final AtomicLong atomicLong = new AtomicLong(0L);

        void add(long count) {
            atomicLong.addAndGet(count);
        }

        long getAndReset() {
            return atomicLong.getAndSet(0L);
        }
    }


    public static class MetricInstance {


        private Map<String, Latency> latencies;
        private Map<String, Counter> counters;
        private Map<String, Traffic> traffics;

        MetricInstance(String[] latencyNames, String[] counterNames, String[] trafficNames) {
            latencies = new HashMap<>(latencyNames.length);
            Arrays.stream(latencyNames).forEach(name -> latencies.put(name, new Latency()));
            counters = new HashMap<>(counterNames.length);
            Arrays.stream(counterNames).forEach(name -> counters.put(name, new Counter()));
            traffics = new HashMap<>(trafficNames.length);
            Arrays.stream(trafficNames).forEach(name -> traffics.put(name, new Traffic()));
        }

        public void addLatency(String name, long value) {
            latencies.get(name).add(value);
        }

        public void addCounter(String name, long value) {
            counters.get(name).add(value);
        }

        public void addTraffic(String name, long value) {
            traffics.get(name).add(value);
        }

        public List<Long> getAndResetLatencies(String name) {
            return latencies.get(name).getAndReset();
        }

        public long getAndResetCounter(String name) {
            return counters.get(name).getAndReset();
        }

        public long getAndResetTraffic(String name) {
            return traffics.get(name).getAndReset();
        }

    }
}