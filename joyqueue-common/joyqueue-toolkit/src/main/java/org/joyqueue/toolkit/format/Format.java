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
package org.joyqueue.toolkit.format;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author liyue25
 * Date: 2019-01-17
 */
public class Format {
    private static final long K = 1024,M = K * K;
    private static final long G = K * M, T = K * G;
    private static final Map<String,Long> UNIT_MAP = new HashMap<>(4);
    static {
        UNIT_MAP.put("k",K);
        UNIT_MAP.put("m",M);
        UNIT_MAP.put("g",G);
        UNIT_MAP.put("t",T);
    }

    private static ThreadLocal<SimpleDateFormat> sdfHolder
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static String format(Date date) {
        return sdfHolder.get().format(date);
    }

    public static String formatWithComma(long position) {
        return NumberFormat.getNumberInstance(Locale.US).format(position);
    }

    public static String formatTraffic(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * copied from https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc/5599842#5599842
     */
    public static String formatSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long parseSize(String sizeString,long defaultValue){
        long size = defaultValue;
        if(sizeString != null ) {
            String trimString = sizeString.trim().toLowerCase();
            if(!trimString.isEmpty()) {
                long unit = UNIT_MAP.getOrDefault(trimString.substring(sizeString.length() - 1),1L);
                if(unit > 1L) {
                    trimString = trimString.substring(0, trimString.length() - 1).trim();
                }
                size = Long.parseLong(trimString) * unit;
            }
        }
        return size;
    }

    public static int getPercentage(String pctString) {
        int pct = -1;

        if (pctString != null && pctString.trim().endsWith("%")) {
            String trimString = pctString.trim();
            trimString = trimString.substring(0, trimString.length() -1);
            pct = Integer.parseInt(trimString);
        }
        return pct;
    }
}