package com.jd.journalq.model.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据库基类
 * Created by chenyanying3 on 18-9-16.
 */
public class BaseModel implements Serializable, Cloneable {

    public static final int ENABLED = 1;

    public static final int DISABLED = 0;

    public static final int DELETED = -1;

    protected long id;

    protected Identity createBy;

    protected Date createTime;

    protected Identity updateBy;

    protected Date updateTime;

    /**
     * 状态(默认启用)
     */
    protected int status = ENABLED;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Identity getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Identity createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Identity getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Identity updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @EnumType(Status.class)
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String toString() {
        return "BaseModel{" +
                "id=" + id +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                '}';
    }

    /**
     * 状态
     */
    public enum Status implements EnumItem {
        ENABLE(ENABLED, "启用"),
        DISABLE(DISABLED, "禁用"),
        DELETE(DELETED, "删除");

        private int value;
        private String description;

        Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        public String description() {
            return description;
        }

    }
    /**
     * 构建器
     *
     * @param <M>
     * @param <T>
     */
    public static abstract class Builder<M extends BaseModel, T> {
        protected M model;

        public Builder() {
        }

        public Builder(M model) {
            this.model = model;
        }

        public T id(long id) {
            model.setId(id);
            return (T) this;
        }

        public T createTime(Date createTime) {
            model.setCreateTime(createTime);
            return (T) this;
        }

        public T createBy(Identity createBy) {
            model.setCreateBy(createBy);
            return (T) this;
        }

        public T updateTime(Date updateTime) {
            model.setUpdateTime(updateTime);
            return (T) this;
        }

        public T updateBy(Identity updateBy) {
            model.setUpdateBy(updateBy);
            return (T) this;
        }

        public T status(int status) {
            model.setStatus(status);
            return (T) this;
        }

        /**
         * 构造对象
         *
         * @return 对象
         */
        public M create() {
            return model;
        }

    }
}
