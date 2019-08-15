package io.chubao.joyqueue.repository;


import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QOperLog;
import org.springframework.stereotype.Repository;

/**
 * @author liyubo4
 * @create 2017-12-07 12:39
 **/
@Repository
public interface OperLogRepository extends PageRepository<OperLog, QOperLog> {
}
