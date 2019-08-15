package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.Query;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public interface NsrService<M,Q extends Query,S> {

    M findById(S s) throws Exception;

    PageResult<M> findByQuery(QPageQuery<Q> query) throws Exception;

    int delete(M model) throws Exception;

    int add(M model) throws Exception;

    int update(M model) throws Exception;

    List<M> findByQuery(Q query) throws Exception;
}
