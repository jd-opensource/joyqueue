package com.jd.journalq.service;

import com.jd.journalq.model.domain.BaseModel;
import com.jd.journalq.model.exception.RepositoryException;
import com.jd.journalq.common.model.ListQuery;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.common.model.Query;

import java.util.List;

/**
 * 服务接口
 * Created by chenyanying3 on 2018-10-15.
 */
public interface PageService<M extends BaseModel, Q extends Query> extends Service<M> {
    /**
     * 分页查询
     *
     * @param query 分页查询条件
     * @return 分页数据
     * @throws RepositoryException
     */
    PageResult<M> findByQuery(QPageQuery<Q> query) throws RepositoryException;
    /**
     * 根据条件查询
     *  @param query 查询条件
     ** @return 匹配的实体列表
     */
    List<M> findByQuery(ListQuery<Q> query);

}
