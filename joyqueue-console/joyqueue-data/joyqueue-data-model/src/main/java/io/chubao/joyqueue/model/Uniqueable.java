package io.chubao.joyqueue.model;


/**
 * 唯一性校验仓库
 *
 * @param <M> 实体对象类型
 */
public interface Uniqueable<M> {

    /**
     * 是否存在
     *
     * @param model 实体对象
     * @return 存在标示
     */
    M exists(M model);

}