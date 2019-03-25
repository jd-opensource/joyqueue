package com.jd.journalq.toolkit.config;

/**
 * property 定义
 */
public interface PropertyDef {
    /**
     * get property name
     *
     * @return
     */
    String getName();

    /**
     * get property value
     *
     * @return
     */
    Object getValue();

    /**
     * get value type
     *
     * @return
     */
    PropertyDef.Type getType();


    enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, DOUBLE, OBJECT
    }
}
