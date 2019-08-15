package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.BaseModel;

/**
 * 服务接口
 * Created by yangyang115 on 18-7-26.
 */
public interface Service<M extends BaseModel> {
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
     * 删除
     *
     * @param id
     * @return
     */
    int deleteById(long id);

    /**
     * 删除
     *
     * @param model
     * @return
     */
    int delete(M model);

    /**
     * 更新状态
     * @param model：必须有id，status，updateBy, updateTime信息
     * @return
     */
    int updateStatus(M model);

}
