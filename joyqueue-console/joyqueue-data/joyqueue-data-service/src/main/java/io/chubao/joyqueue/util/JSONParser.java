package io.chubao.joyqueue.util;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.monitor.RestResponse;
import io.chubao.joyqueue.monitor.RestResponseCode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author wangjin
 * @time 2018-12-03
 * @mail wangjin18@jd.com
 *
 **/
public class JSONParser {
    /**
     * @param list true indicate body is a list<dataClass> or
     * @return response
     * @throws ServiceException when response not success
     **/
    public static <T> RestResponse<T> parse(String content, Class restResponse, Class dataClass, boolean list) {
        RestResponse<T> response = null;
        try {
            if (list) {
                response = parse(content,type(restResponse,type(List.class,dataClass)));
            } else {
                response = parse(content,type(restResponse,dataClass));
            }
            if (response.getCode() != RestResponseCode.SUCCESS.getCode()) {
                throw new ServiceException(response.getCode(), response.getMessage());
            }
        } catch (Exception e) {
        }
        return response;
    }

    public static <T> RestResponse<T> parse(String content,Type type) {
        RestResponse<T> response = null;
        try {
            response = JSON.parseObject(content, type);
            if (response.getCode() != RestResponseCode.SUCCESS.getCode()) {
                throw new ServiceException(response.getCode(), response.getMessage());
            }
        } catch (Exception e) {
        }
        return response;
    }

    public static Type type(final Class<?> raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }
}
