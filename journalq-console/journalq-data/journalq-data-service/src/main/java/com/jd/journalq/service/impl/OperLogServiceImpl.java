package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.query.QOperLog;
import com.jd.journalq.repository.OperLogRepository;
import com.jd.journalq.service.OperLogService;
import org.springframework.stereotype.Service;

/**
 * @author liyubo4
 * @create 2017-12-07 18:52
 **/
@Service("operLogService")
public class OperLogServiceImpl extends PageServiceSupport<OperLog, QOperLog,OperLogRepository> implements OperLogService {

}
