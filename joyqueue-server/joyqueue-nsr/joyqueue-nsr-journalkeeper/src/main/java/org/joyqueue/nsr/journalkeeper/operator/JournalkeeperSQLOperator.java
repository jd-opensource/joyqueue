package org.joyqueue.nsr.journalkeeper.operator;

import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.client.support.DefaultSQLOperator;
import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.operator.ResultSet;
import org.joyqueue.nsr.sql.operator.SQLOperator;

/**
 * JournalkeeperSQLOperator
 * author: gaohaoxiang
 * date: 2020/8/12
 */
public class JournalkeeperSQLOperator implements SQLOperator {

    private SQLClient sqlClient;
    private io.journalkeeper.sql.client.SQLOperator journalkeeperSQLOperator;

    public JournalkeeperSQLOperator(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
        this.journalkeeperSQLOperator = new DefaultSQLOperator(this.sqlClient);
    }

    @Override
    public Object insert(String sql, Object... params) {
        return journalkeeperSQLOperator.insert(sql, params);
    }

    @Override
    public int update(String sql, Object... params) {
        return journalkeeperSQLOperator.update(sql, params);
    }

    @Override
    public int delete(String sql, Object... params) {
        return journalkeeperSQLOperator.delete(sql, params);
    }

    @Override
    public ResultSet query(String sql, Object... params) {
        io.journalkeeper.sql.client.domain.ResultSet resultSet = journalkeeperSQLOperator.query(sql, params);
        if (resultSet == null) {
            return null;
        }
        return new ResultSet(resultSet.getRows());
    }

    @Override
    public BatchSQLOperator beginBatch() {
        return new JournalkeeperBatchSQLOperator(journalkeeperSQLOperator.beginBatch());
    }
}