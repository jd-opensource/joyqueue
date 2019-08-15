package io.chubao.joyqueue.repository;

import io.chubao.joyqueue.model.domain.BaseModel;

/**
 * 仓库公共接口
 * Created by yangyang115 on 18-7-26.
 */
public interface Repository<M extends BaseModel> {

    /**
     * 根据ID查找
     *
     * @param id
     * @return
     */
    M findById(long id);

    /**
     * 增加
     *
     * @param model
     * @return
     */
    int add(M model);

    /**
     * 修改
     *
     * @param  model：必须有id信息
     * @return
     */
    int update(M model);

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    int deleteById(long id);

    /**
     * 更改状态
     *
     * @param  model：必须有id信息
     * @return
     */
    int state(M model);

}
