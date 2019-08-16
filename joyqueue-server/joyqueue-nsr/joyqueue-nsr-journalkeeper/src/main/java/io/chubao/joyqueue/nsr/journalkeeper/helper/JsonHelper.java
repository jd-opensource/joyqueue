package io.chubao.joyqueue.nsr.journalkeeper.helper;

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