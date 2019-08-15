package io.chubao.joyqueue.datasource;


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
    io.chubao.joyqueue.datasource.XDataSource build(io.chubao.joyqueue.datasource.DataSourceConfig config);

}
