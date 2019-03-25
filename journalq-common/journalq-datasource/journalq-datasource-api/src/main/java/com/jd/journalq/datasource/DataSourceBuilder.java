package com.jd.journalq.datasource;


import com.jd.laf.extension.Type;

/**
 * 数据源构造器
 */
public interface DataSourceBuilder extends Type {

    /**
     * 创建连接池
     *
     * @param config 连接池配置
     * @return 连接池
     */
    XDataSource build(DataSourceConfig config);

}
