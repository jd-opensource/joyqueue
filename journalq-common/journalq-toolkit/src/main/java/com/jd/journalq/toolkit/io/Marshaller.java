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
package com.jd.journalq.toolkit.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化接口
 * Created by hexiaofeng on 16-5-6.
 */
public interface Marshaller {

    /**
     * 序列化
     *
     * @param object 对象
     * @param out    输出流
     * @throws MarshallException
     */
    void marshal(Object object, OutputStream out) throws MarshallException;

    /**
     * 反序列化
     *
     * @param clazz 目标类型
     * @param in    输入流
     * @return
     * @throws MarshallException
     */
    <T> T unmarshal(Class<T> clazz, InputStream in) throws MarshallException;


    /**
     * 序列化异常
     * Created by hexiaofeng on 16-5-6.
     */
    class MarshallException extends IOException {

        public MarshallException() {
        }

        public MarshallException(String message) {
            super(message);
        }

        public MarshallException(String message, Throwable cause) {
            super(message, cause);
        }

        public MarshallException(Throwable cause) {
            super(cause);
        }
    }

}
