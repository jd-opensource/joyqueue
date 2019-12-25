/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service.impl;

import org.joyqueue.exception.ValidationException;
import org.joyqueue.model.Uniqueable;
import org.joyqueue.model.domain.BaseModel;
import org.joyqueue.model.domain.UniqueFields;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.exception.DuplicateKeyException;
import org.joyqueue.model.exception.UniqueException;
import org.joyqueue.repository.Repository;
import org.joyqueue.service.Service;
import org.joyqueue.util.NullUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 业务域基础类
 * Created by chenyanying3 on 2018-10-15.
 */
public abstract class ServiceSupport<M extends BaseModel,R extends Repository<M>> implements Service<M> {
    // 仓库
    @Autowired
    protected R repository;

    /**
     * 添加实体
     *
     * @param model 实体对象
     * @return 实体对象
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int add(final M model){
        if (model == null || (!(model instanceof User) && model.getCreateBy() ==null)) {
            throw new IllegalArgumentException("新增传参错误！");
        }

        if (repository instanceof Uniqueable) {
            if (((Uniqueable<M>) repository).exists(model) != null) {
                throw new ValidationException(ValidationException.UNIQUE_EXCEPTION_STATUS, "应用用户已经存在！");
            }
        }

        if (model.getCreateTime() == null) {
            model.setCreateTime(new Date());
        }
        if(model.getUpdateBy() == null){
            model.setUpdateBy(model.getCreateBy());
        }
        if (model.getUpdateTime() == null) {
            model.setUpdateTime(model.getCreateTime());
        }

        try {
            return repository.add(model);
        } catch (DuplicateKeyException e) {
            // 将DAO异常转换成业务层异常
            throw new UniqueException("已经存在，请检查数据",e);
        }
    }

    /**
     * 更新实体
     *
     * @param model 实体对象
     * @return 受影响的记录条数
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int update(final M model) {
        NullUtil.checkArgumentForUpdate(model);
        model.setUpdateTime(new Date());
        return repository.update(model);
    }

    /**
     * 更新状态实体
     *
     * @param model 实体对象
     * @return 受影响的记录条数
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int updateStatus(final M model) {
        NullUtil.checkArgumentForUpdate(model);
        model.setUpdateTime(new Date());
        return repository.state(model);
    }

    /**
     * 删除实体
     *
     * @param model 实体对象
     * @return 受影响的记录条数
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int delete(final M model) {
        NullUtil.checkArgumentForUpdate(model);
        model.setUpdateTime(new Date());
        model.setStatus(BaseModel.DELETED);
        return repository.state(model);
    }

    /**
     * 根据ID删除实体
     *
     * @param id 实体ID
     * @return 受影响的记录条数
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @Override
    public int deleteById(final long id) {
        if (id <= 0L) {
            throw new IllegalArgumentException("根据ID删除传参错误！");
        }
        return repository.deleteById(id);
    }

    /**
     * 根据ID查询实体
     *
     * @param id 实体唯一id
     * @return 受影响的记录条数
     */
    @Override
    public M findById(final long id) {
        if (id <= 0L) {
            throw new IllegalArgumentException("根据ID查询传参错误！");
        }
        return repository.findById(id);
    }

    /**
     * 实体是否存在
     *
     * @param model 实体对象
     * @return 存在标示
     */
    public M exists(final M model) {
        if (model == null) {
            throw new IllegalArgumentException("判定是否存在传参错误！");
        }
        if (repository instanceof Uniqueable) {
            return ((Uniqueable<M>) repository).exists(model);
        }

        return null;
    }

    /**
     * 新增或修改，不存在，新增，否则，修改
     *
     * @param model 实体对象
     * @return 实体对象
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public M addOrUpdate(final M model) {
        if (model == null) {
            throw new IllegalArgumentException("新增或修改传参错误！");
        }
        // 数据是否已经存在
        M existObj = ((Uniqueable<M>) repository).exists(model);
        if (existObj != null) {// 存在
            update(model);
        } else {// 不存在
            add(model);
        }
        return model;
    }

    protected String getUniqueExceptionMessage() {
        String className = this.getClass().getSimpleName();
        String[] uniqueFields = UniqueFields.valueOf(className.substring(0, className.length()-11)).fields();
        if (uniqueFields == null || uniqueFields.length < 1) {
            return "已经存在";
        } else {
            StringBuffer msg = new StringBuffer("");
            for(String fields : uniqueFields) {
                msg.append(fields).append(",");
            }
            msg.deleteCharAt(msg.length()-1);
            return msg.append("|").append("已经存在").toString();
        }
    }

}
