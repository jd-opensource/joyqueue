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
package org.joyqueue.nsr.util;

import org.joyqueue.toolkit.URL;
import org.joyqueue.toolkit.network.IpUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP规则匹配器，仅支持IP V4
 */
public class IPRangeMatcher implements DCMatcher {
    private static Pattern P_RANGE1 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})$");
    private static Pattern P_RANGE2 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\-([0-9]{1,3})$");
    private static Pattern P_RANGE3 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\-([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})"
            + "\\.([0-9]{1,3})$");
    private static Pattern P_RANGE4 = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})\\.\\*$");
    private String pattern;

    @Override
    public boolean match(String ip) {
        if (ip == null || !IpUtil.isValidIpV4Address(ip)) {
            return false;
        }
        String[] parts = ip.split("[\\.]");
        String[] patts = this.pattern.split("[;,]");
        Matcher matcher;
        long beginIp;
        long endIp;
        for (String patt : patts) {
            patt = patt.trim();
            if (patt.isEmpty()) {
                continue;
            }
            matcher = P_RANGE3.matcher(patt);
            if (matcher.find()) {
                beginIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                endIp = toLong(matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8));
            } else {
                matcher = P_RANGE1.matcher(patt);
                if (matcher.find()) {
                    beginIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), "0");
                    endIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), "255");
                } else {
                    matcher = P_RANGE2.matcher(patt);
                    if (matcher.find()) {
                        beginIp = toLong(matcher.group(1), matcher.group(2), matcher.group(3), "0");
                        endIp = toLong(matcher.group(1), matcher.group(2), matcher.group(4), "255");
                    } else {
                        matcher = P_RANGE4.matcher(patt);
                        if (matcher.find()) {
                            beginIp = toLong(matcher.group(1), matcher.group(2), "0", "0");
                            endIp = toLong(matcher.group(1), matcher.group(2), "255", "255");
                        } else {
                            continue;
                        }
                    }
                }
            }
            long value = toLong(parts[0], parts[1], parts[2], parts[3]);
            if (value >= beginIp && value <= endIp) {
                return true;
            }
        }
        return false;
    }

    private static long toLong(final String p1, final String p2, final String p3, final String p4) {
        long[] ip = new long[4];
        ip[0] = Long.parseLong(p1);
        ip[1] = Long.parseLong(p2);
        ip[2] = Long.parseLong(p3);
        ip[3] = Long.parseLong(p4);
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }


    @Override
    public String type() {
        return "IPRANGE";
    }
    @Override
    public void setUrl(URL url) {
        this.pattern = url.getParameters().get("pattern");
    }

}