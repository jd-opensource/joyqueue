package io.chubao.joyqueue.toolkit.config;

/**
 * 是否感知配置
 */
public interface PropertySupplierAware {
    /**
     * set supplier
     *
     * @param supplier
     */
    void setSupplier(PropertySupplier supplier);
}
