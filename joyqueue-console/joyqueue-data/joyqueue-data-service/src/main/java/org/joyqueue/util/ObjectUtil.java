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
package org.joyqueue.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectUtil {
    private static final Logger logger = LoggerFactory.getLogger(ObjectUtil.class);

    public static final String WHERE_SQL_VARIABLE_REGEX = "#\\{[a-zA-Z]+[a-zA-Z0-9\\.]*[a-zA-Z0-9]+\\}";
    public static final String WHERE_SQL_VARIABLE_REGEX_PREFIX = "#\\{";
    public static final String WHERE_SQL_VARIABLE_REGEX_SUFFIX = "\\}";

    public static Map<String, Object> flatMap(Object obj) {
        Preconditions.checkArgument(obj != null, "invalid arg.");
        Map<String, Object> map = (JSONObject) JSON.toJSON(obj);
        return flatMap(map);
    }

    public static Map<String, Object> flatMap(Map<String, Object> map) {
        Preconditions.checkArgument(map != null, "invalid arg.");
        Map<String, Object> flatMap = new HashMap<>();
        map.entrySet().forEach(entry-> {
            Object value = entry.getValue();
            if (value != null && value instanceof Map) {
                ((Map<String, Object>) entry.getValue()).entrySet().forEach(subEntry ->
                        flatMap.put(entry.getKey() + "." + subEntry.getKey(), subEntry.getValue())
                );
            } else {
                flatMap.put(entry.getKey(), value);
            }
        });
        return flatMap;
    }

    public static String replaceSql(Object obj, String sql) {
        Preconditions.checkArgument(obj != null && sql != null, "invalid arg.");
        Map<String, Object> flatMap = flatMap(obj);
        return replaceSql(flatMap, sql);
    }
    
    public static List<String> findVariableFromSql(String sql) {
        Preconditions.checkArgument(sql != null, "invalid arg.");
        String result = sql;
        List<String> variables = new ArrayList<>();
        Pattern pattern = Pattern.compile(WHERE_SQL_VARIABLE_REGEX);
        Matcher matcher = pattern.matcher(sql);
        while(matcher.find()) {
            String variable = matcher.group(0);
            variable = variable.substring(2, variable.length() - 1);
            variables.add(variable);
            result = result.replaceAll(WHERE_SQL_VARIABLE_REGEX_PREFIX+variable+WHERE_SQL_VARIABLE_REGEX_SUFFIX, "");
        }
        return variables;
    }

    public static String replaceSql(Map<String, Object> flatMap, String sql) {
        Preconditions.checkArgument(flatMap != null && sql != null, "invalid arg.");
        //Find fields needed to be replaced and replace all
        String result = sql;
        Pattern pattern = Pattern.compile(WHERE_SQL_VARIABLE_REGEX);
        Matcher matcher = pattern.matcher(sql);
        while(matcher.find()) {
            String variable = matcher.group(0);
            variable = variable.substring(2, variable.length()-1);
            result = result.replaceAll(WHERE_SQL_VARIABLE_REGEX_PREFIX+variable+WHERE_SQL_VARIABLE_REGEX_SUFFIX,
                    String.valueOf(flatMap.get(variable)));
        }

        return result;
    }

    public static Map<String, Object> combineAndFlatProperties(Map<String, Object> dest, Object source) {
        Preconditions.checkArgument(dest != null && source != null, "invalid arg.");
        Map<String, Object> sourceFlatMap = flatMap(source);
        Map<String, Object> destFlatMap = flatMap(dest);
        if (null == sourceFlatMap | sourceFlatMap.isEmpty()) {
            return destFlatMap;
        }
        sourceFlatMap.entrySet().forEach(entry->destFlatMap.put(entry.getKey(), entry.getValue()));
        return destFlatMap;
    }

    public static Map<String, String> transformCommaStringToMap(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        Map<String, String> dest = new HashMap<>();
        for(String key: source.split(";")) {
            String[] keyValue = key.split(":");
            dest.put(keyValue[0], keyValue[1]);
        }
        return dest;
    }
}
