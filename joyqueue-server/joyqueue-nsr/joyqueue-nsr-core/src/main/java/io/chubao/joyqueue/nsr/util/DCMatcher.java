package io.chubao.joyqueue.nsr.util;


import io.chubao.joyqueue.toolkit.URL;
import com.jd.laf.extension.Type;

/**
 * 数据中心匹配器
 */
public interface DCMatcher extends Type<String> {
    /**
     * 根据IP进行匹配
     *
     * @param ip
     * @return
     */
    boolean match(String ip);
    /**
     * 设置匹配规则
     *
     * @param url
     * @return
     */
    void setUrl(URL url);
}