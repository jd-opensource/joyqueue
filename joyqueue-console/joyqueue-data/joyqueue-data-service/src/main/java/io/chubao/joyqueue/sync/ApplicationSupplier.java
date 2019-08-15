package io.chubao.joyqueue.sync;

public interface ApplicationSupplier {

    /**
     * 根据应用代码查找应用
     *
     * @param appCode 应用
     * @param source 来源
     * @return
     */
    ApplicationInfo findByCode(String appCode, int source) throws Exception;
}
