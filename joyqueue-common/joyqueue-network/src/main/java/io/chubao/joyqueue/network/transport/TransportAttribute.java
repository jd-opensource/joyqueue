package io.chubao.joyqueue.network.transport;

import java.util.Set;

/**
 * TransportAttribute
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public interface TransportAttribute {

    <T> T set(Object key, Object value);

    <T> T putIfAbsent(Object key, Object value);

    <T> T get(Object key);

    <T> T remove(Object key);

    boolean contains(Object key);

    Set<Object> keys();
}