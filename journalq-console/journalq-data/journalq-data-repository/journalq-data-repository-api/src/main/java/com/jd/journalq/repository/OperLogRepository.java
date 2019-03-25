package com.jd.journalq.repository;


import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.query.QOperLog;
import org.springframework.stereotype.Repository;

/**
 * @author liyubo4
 * @create 2017-12-07 12:39
 **/
@Repository
public interface OperLogRepository extends PageRepository<OperLog, QOperLog> {
}
