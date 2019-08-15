package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.query.QOperLog;
import io.chubao.joyqueue.repository.OperLogRepository;
import io.chubao.joyqueue.service.OperLogService;
import org.springframework.stereotype.Service;

/**
 * @author liyubo4
 * @create 2017-12-07 18:52
 **/
@Service("operLogService")
public class OperLogServiceImpl extends PageServiceSupport<OperLog, QOperLog,OperLogRepository> implements OperLogService {

}
