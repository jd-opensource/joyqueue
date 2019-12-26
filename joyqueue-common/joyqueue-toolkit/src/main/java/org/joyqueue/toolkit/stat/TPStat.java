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
package org.joyqueue.toolkit.stat;

import java.io.Serializable;

/**
 * TP性能统计
 */
public class TPStat implements Serializable {
    // TP999
    private int tp999;
    // TP99
    private int tp99;
    // TP90
    private int tp90;
    // TP50
    private int tp50;
    // 最大时间
    private int max;
    // 最小时间
    private int min;
    // 成功调用次数
    private long success;
    // 出错调用次数
    private long error;
    // 成功处理的记录条数(用于批量)
    private long count;
    // 成功处理的数据包大小
    private long size;
    // 成功处理的时间
    private long time;

    public TPStat() {
    }

    public TPStat(long count, long success, long error, long size, long time) {
        this.count = count;
        this.success = success;
        this.error = error;
        this.size = size;
        this.time = time;
    }

    public int getTp999() {
        return tp999;
    }

    public void setTp999(int tp999) {
        this.tp999 = tp999;
    }

    public int getTp99() {
        return tp99;
    }

    public void setTp99(int tp99) {
        this.tp99 = tp99;
    }

    public int getTp90() {
        return tp90;
    }

    public void setTp90(int tp90) {
        this.tp90 = tp90;
    }

    public int getTp50() {
        return tp50;
    }

    public void setTp50(int tp50) {
        this.tp50 = tp50;
    }

    public int getAvg() {
        return success <= 0 ? 0 : (int) Math.ceil(time * 1.0 / success);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getError() {
        return error;
    }

    public void setError(long error) {
        this.error = error;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getRatio() {
        return error <= 0 ? 100.0 : ((long) (success * 1.0 / (success + error) * 10000) / 100.0);
    }

    public long getTps() {
        return success <= 0 ? 0 : (time <= 0 ? success * 1000 : success * 1000 / time);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("\"tp999\":").append(tp999);
        sb.append(", \"tp99\":").append(tp99);
        sb.append(", \"tp90\":").append(tp90);
        sb.append(", \"tp50\":").append(tp50);
        sb.append(", \"avg\":").append(getAvg());
        sb.append(", \"max\":").append(max);
        sb.append(", \"min\":").append(min);
        sb.append(", \"count\":").append(success);
        sb.append(", \"error\":").append(error);
        sb.append(", \"size\":").append(size);
        sb.append(", \"time\":").append(time);
        sb.append(", \"ratio\":").append(getRatio());
        sb.append('}');
        return sb.toString();
    }
}
