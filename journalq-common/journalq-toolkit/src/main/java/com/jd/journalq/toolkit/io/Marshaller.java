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
