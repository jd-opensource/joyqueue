package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.BaseModel;
import io.chubao.joyqueue.model.exception.RepositoryException;
import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.Query;

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
