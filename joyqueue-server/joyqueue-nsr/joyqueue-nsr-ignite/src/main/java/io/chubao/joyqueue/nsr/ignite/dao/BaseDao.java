package io.chubao.joyqueue.nsr.ignite.dao;

import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.Query;

import java.util.List;


public interface BaseDao<M, Q extends Query, K> {
    /**
     * 根据ID查找
     *
     * @param id
     * @return
     */
    M findById(K id);

    /**
     * 增加
     *
     * @param model
     * @return
     */
    void add(M model);

    /**
     * 修改
     *
     * @param model：必须有id信息
     * @return
     */
    void addOrUpdate(M model);


    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    void deleteById(K id);

    /**
     * 分页查找
     *
     * @param pageQuery
     * @return
     */
    PageResult<M> pageQuery(QPageQuery<Q> pageQuery);

    /**
     * 查找全部
     *
     * @return
     */
    List<M> list(Q query);
}
