/**
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
package com.jd.joyqueue.toolkit.stat;

import com.jd.joyqueue.toolkit.concurrent.LoopThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 性能抖动分析工具（平均响应时间,调用次数,TP99,TP90,MAX）
 * <p>
 * Created by chengzhiliang on 2019/3/25.
 */
public class TPStatUtil {
    private static final Logger logger = LoggerFactory.getLogger(TPStatUtil.class);

    private static Map<String, Queue<Double>> monitorMap = new ConcurrentHashMap<>();

    private static LoopThread calcThread = LoopThread.builder()
            .name("Analyze-TP-TPStatUtil")
            .sleepTime(1000, 1000)
            .doWork(TPStatUtil::execute)
            .onException(e -> logger.error("", e)).build();

    static {
        calcThread.start();
    }

    private static void execute() {
        Set<String> monitorKeySet = monitorMap.keySet();
        monitorKeySet.stream().forEach(key -> {
            Queue<Double> queue = monitorMap.get(key);
            monitorMap.put(key, new ConcurrentLinkedQueue<>());
            analyze(key, queue);
        });
    }

    public static void append(String monitorKey, long startTime, long endTime) {
        long value = endTime - startTime;
        double result = value / 1000000.0;
        Queue<Double> queue = getOrCreate(monitorKey);
        queue.add(result);
    }

    private static Queue<Double> getOrCreate(String monitorKey) {
        Queue<Double> queue = monitorMap.get(monitorKey);
        if (queue == null) {
            synchronized (TPStatUtil.class) {
                if ((queue = monitorMap.get(monitorKey)) == null) {
                    queue = new ConcurrentLinkedQueue<>();
                    monitorMap.put(monitorKey, queue);
                }
            }
        }

        return queue;
    }

    private static void analyze(String monitorKey, Queue<Double> queue) {
        double[] tempArr = queue.parallelStream().mapToDouble(Double::doubleValue).sorted().toArray();
        if (tempArr.length == 0) {
            logger.info("TPStatUtil have no element to analyze!");
            return;
        }
        double average = queue.parallelStream().mapToDouble(Double::doubleValue).average().orElse(0); // 平均响应时间
        long count = tempArr.length; // 次数
        double max = tempArr[(int) (count - 1)]; // 最大值
        double tp99 = tempArr[(int) (count * 0.99d)]; // tp99
        double tp90 = tempArr[(int) (count * 0.90d)]; // tp90

        StringBuilder report = new StringBuilder();
        report.append("statType:" + monitorKey);
        report.append(", ");
        report.append("avg:" + average);
        report.append(", ");
        report.append("count:" + count);
        report.append(", ");
        report.append("max:" + max);
        report.append(", ");
        report.append("tp99:" + tp99);
        report.append(", ");
        report.append("tp90:" + tp90);

        logger.info(report.toString());

        queue.clear();
    }

}