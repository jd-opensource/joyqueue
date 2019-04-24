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
package com.jd.journalq.model.util;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("ALL")
public class DataConvertUtil {

    public static String convertStringTreeSetToString(Set<String> sources) {
        if (sources == null) {
            return null;
        }

        StringBuffer dest = new StringBuffer("");
        sources.forEach(source -> dest.append(source).append(","));
        dest.deleteCharAt(dest.length()-1);
        return dest.toString();
    }

    public static String convertIntegerTreeSetToString(Set<Integer> sources) {
        if (sources == null) {
            return null;
        }

        StringBuffer dest = new StringBuffer("");
        sources.forEach(source -> dest.append(source).append(","));
        dest.deleteCharAt(dest.length()-1);
        return dest.toString();
    }

    public static Set<String> convertStringToTreeSet(String source) {
        if (StringUtils.isBlank(source)) {
            return Collections.EMPTY_SET;
        }
        if ((source.startsWith("(") && source.endsWith(")")) || (source.startsWith("[") && source.endsWith("]"))) {
            source = source.substring(1, source.length()-1);
        }
        Set<String> dest = new TreeSet<>();
        String[] sources = source.split("|");
        for (String src : sources) {
            dest.add(src);
        }
        return dest;
    }

    public static long convertStringDateToTimestamp(String source) throws ParseException {
//        String source = "2015-12-7T16:00:00.000Z";
        source = source.replace("Z", " UTC");//注意是空格+UTC
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//注意格式化的表达式
        return format.parse(source).getTime()/1000;//秒
    }

    public static void main(String[] args) {
        Set<String> set1 = new TreeSet<>();
        set1.add("hh1");
        set1.add("hh2");
        set1.add("hh3");
        String result = set1.toString();

        String source = "(10\\.0\\.0\\.1\\\\50080[11223344]|10\\.0\\.0\\.1_50080[11223344])";
        source = source.replaceAll("\\\\", "");

    }

}
