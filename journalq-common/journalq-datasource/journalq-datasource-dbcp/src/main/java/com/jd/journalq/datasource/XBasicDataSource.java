package com.jd.journalq.datasource;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * dbcp数据源
 */
public class XBasicDataSource extends BasicDataSource implements XDataSource {

    @Override
    public void destroy() {
        try {
            close();
        } catch (SQLException ignored) {
        }
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
