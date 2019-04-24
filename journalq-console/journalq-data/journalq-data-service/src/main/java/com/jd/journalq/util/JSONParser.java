/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.journalq.monitor.RestResponse;
import com.jd.journalq.monitor.RestResponseCode;
import com.jd.journalq.exception.ServiceException;

import java.io.IOException;
import java.util.List;

/**
 * @author wangjin
 * @time 2018-12-03
 * @mail wangjin18@jd.com
 *
 **/
public class JSONParser {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T parse(String content, Class restClass, Class dataClass) throws IOException {
        JavaType javaType = composite(restClass, dataClass);
        return mapper.readValue(content, javaType);
    }

    public static <T> T parseList(String content, Class restClass, Class dataClass) throws IOException {
        JavaType javaType = compositeList(restClass, dataClass);
        return mapper.readValue(content, javaType);
    }


    /**
     *  composite class a and b to a Java Type
     *
     **/
    public static JavaType composite(Class a, Class b) {
        return mapper.getTypeFactory().constructParametricType(a, b);
    }


    /**
     *
     *composite list
     **/
    private static JavaType compositeList(Class a, Class listClass) {
        JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, listClass);
        return mapper.getTypeFactory().constructParametricType(a, listType);
    }


    /**
     * @param list true indicate body is a list<dataClass> or
     * @return response
     * @throws ServiceException when response not success
     **/
    public static <T> RestResponse<T> parse(String content, Class restResponse, Class dataClass, boolean list) {
        RestResponse<T> response = null;
        try {
            if (list) {
                response = JSONParser.parseList(content, restResponse, dataClass);
            } else {
                response = JSONParser.parse(content, restResponse, dataClass);
            }
            if (response.getCode() != RestResponseCode.SUCCESS.getCode()) {
                throw new ServiceException(response.getCode(), response.getMessage());
            }
        } catch (IOException e) {
        }
        return response;
    }


}
