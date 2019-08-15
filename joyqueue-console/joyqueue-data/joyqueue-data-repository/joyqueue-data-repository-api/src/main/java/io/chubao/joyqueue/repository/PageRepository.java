package io.chubao.joyqueue.repository;

import io.chubao.joyqueue.model.domain.BaseModel;
import io.chubao.joyqueue.model.exception.RepositoryException;
import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.Query;

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
