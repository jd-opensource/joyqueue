package io.chubao.joyqueue.broker.monitor.converter;

import com.jd.laf.extension.Type;

/**
 * @author lining11
 * Date: 2018/12/20
 */
public interface Converter<T, R> extends Type {
    R convert(T t);
}
