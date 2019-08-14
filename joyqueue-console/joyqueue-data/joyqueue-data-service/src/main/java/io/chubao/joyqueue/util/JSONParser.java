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
