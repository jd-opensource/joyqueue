package com.jd.journalq.datasource;

import javax.sql.DataSource;

/**
 * 数据源接口
 */
public interface XDataSource extends DataSource {

    /**
     * 销毁
     */
    void destroy();

}
