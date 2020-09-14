package org.joyqueue.nsr.journalkeeper.repository;

import org.joyqueue.monitor.PointTracer;
import org.joyqueue.nsr.journalkeeper.JournalkeeperBatchOperationContext;
import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.operator.SQLOperator;
import org.joyqueue.nsr.sql.repository.BaseRepository;

/**
 * JournalkeeperBaseRepository
 * author: gaohaoxiang
 * date: 2020/8/14
 */
public class JournalkeeperBaseRepository extends BaseRepository {

    public JournalkeeperBaseRepository(SQLOperator sqlOperator, PointTracer tracer) {
        super(sqlOperator, tracer);
    }

    @Override
    protected BatchSQLOperator getBatchSQLOperator() {
        return JournalkeeperBatchOperationContext.getBatchSQLOperator();
    }
}
