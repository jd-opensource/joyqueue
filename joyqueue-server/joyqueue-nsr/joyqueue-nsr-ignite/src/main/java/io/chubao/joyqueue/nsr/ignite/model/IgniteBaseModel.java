package io.chubao.joyqueue.nsr.ignite.model;

import java.io.Serializable;

/**
 * @author lixiaobin6
 * 下午2:54 2018/8/16
 */
public interface IgniteBaseModel extends Serializable {
    String SCHEMA = "joyqueue";
    String SPLICE = ".";
    String SEPARATOR_SPLIT = "\\.";

    Object getId();
}
