package com.jd.journalq.datasource;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Hikari 数据源
 */
public class HikariXDataSource extends HikariDataSource implements XDataSource {

    @Override
    public void destroy() {
        close();
    }
}
