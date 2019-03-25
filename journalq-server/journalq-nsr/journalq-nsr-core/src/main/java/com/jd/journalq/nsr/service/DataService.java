package com.jd.journalq.nsr.service;


import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.Query;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/11/8
 */
public interface DataService<T, Q extends Query, K> {
    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    T getById(K id);

    /**
     * 根据model获取
     *
     * @param model
     * @return
     */
    T get(T model);

    /**
     * 添加或者更新
     *
     * @param t
     */
    void addOrUpdate(T t);

    /**
     * 根据ID删除
     *
     * @param id
     */
    void deleteById(K id);

    /**
     * 根据model删除
     *
     * @param model
     */
    void delete(T model);

    /**
     * 获取所有
     *
     * @return
     */
    List<T> list();

    /**
     * 获取所有
     *
     * @return
     */
    List<T> list(Q query);

    /**
     * 分页查询
     *
     * @param pageQuery
     * @return
     */
    PageResult<T> pageQuery(QPageQuery<Q> pageQuery);
}
