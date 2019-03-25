package com.jd.journalq.repository;

import com.jd.journalq.model.domain.BaseModel;
import com.jd.journalq.model.exception.RepositoryException;
import com.jd.journalq.common.model.ListQuery;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.common.model.Query;

import java.util.List;

/**
 * 仓库公共接口（带分页）
 * Created by chenyanying3 on 18-10-15.
 */
public interface PageRepository<M extends BaseModel, Q extends Query> extends Repository<M> {

    /**
     * 分页查询
     *
     * @param query 分页查询条件
     * @return 分页数据
     * @throws RepositoryException
     */
    PageResult<M> findByQuery(QPageQuery<Q> query) throws RepositoryException;


    /**
     * 查询
     * @param query
     * @return
     */
    List<M> findByQuery(ListQuery query);
}
