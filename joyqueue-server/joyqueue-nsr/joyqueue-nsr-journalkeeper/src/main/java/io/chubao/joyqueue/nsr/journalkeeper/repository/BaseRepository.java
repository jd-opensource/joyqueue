package io.chubao.joyqueue.nsr.journalkeeper.repository;

import io.chubao.joyqueue.nsr.journalkeeper.TransactionContext;
import io.chubao.joyqueue.nsr.journalkeeper.helper.ResultSetHelper;
import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.client.SQLTransactionOperator;
import io.journalkeeper.sql.client.domain.ResultSet;

import java.util.List;

/**
 * BaseRepository
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class BaseRepository {

    private SQLOperator sqlOperator;

    public BaseRepository(SQLOperator sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

    public String insert(String sql, Object... params) {
        SQLTransactionOperator transactionOperator = TransactionContext.getTransactionOperator();
        if (transactionOperator != null) {
            try {
                return transactionOperator.insert(sql, params);
            } catch (Exception e) {
                TransactionContext.close();
                throw e;
            }
        } else {
            return sqlOperator.insert(sql, params);
        }
    }

    public int update(String sql, Object... params) {
        SQLTransactionOperator transactionOperator = TransactionContext.getTransactionOperator();
        if (transactionOperator != null) {
            try {
                return transactionOperator.update(sql, params);
            } catch (Exception e) {
                TransactionContext.close();
                throw e;
            }
        } else {
            return sqlOperator.update(sql, params);
        }
    }

    public int delete(String sql, Object... params) {
        SQLTransactionOperator transactionOperator = TransactionContext.getTransactionOperator();
        if (transactionOperator != null) {
            try {
                return transactionOperator.delete(sql, params);
            } catch (Exception e) {
                TransactionContext.close();
                throw e;
            }
        } else {
            return sqlOperator.delete(sql, params);
        }
    }

    public ResultSet query(String sql, Object... params) {
        SQLTransactionOperator transactionOperator = TransactionContext.getTransactionOperator();
        if (transactionOperator != null) {
            try {
                return transactionOperator.query(sql, params);
            } catch (Exception e) {
                TransactionContext.close();
                throw e;
            }
        } else {
            return sqlOperator.query(sql, params);
        }
    }

    public int count(String sql, Object... params) {
        ResultSet resultSet = query(sql, params);
        return Integer.valueOf(resultSet.getRows().get(0).entrySet().iterator().next().getValue());
    }

    public <T> List<T> query(Class<T> type, String sql, List params) {
        Object[] array = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            array[i] = params.get(i);
        }
        return query(type, sql, array);
    }

    public <T> T queryOnce(Class<T> type, String sql, List params) {
        Object[] array = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            array[i] = params.get(i);
        }
        return queryOnce(type, sql, array);
    }

    public <T> List<T> query(Class<T> type, String sql, Object... params) {
        ResultSet resultSet = query(sql, params);
        return ResultSetHelper.assembleList(type, resultSet);
    }

    public <T> T queryOnce(Class<T> type, String sql, Object... params) {
        ResultSet resultSet = query(sql, params);
        return ResultSetHelper.assembleOnce(type, resultSet);
    }
}