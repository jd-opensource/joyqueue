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
package org.joyqueue.nsr.sql.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JsonHelper
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JsonHelper {

    public static String toJson(Object value) {
        return JSON.toJSONString(value,
                SerializerFeature.PrettyFormat, SerializerFeature.DisableCircularReferenceDetect);
    }

    public static <T> T parseJson(Class<T> type, String json) {
        return JSON.parseObject(json, type);
    }
}