package com.jd.journalq.service;

import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.domain.TopicUnsubscribedApplication;
import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.model.QPageQuery;

import java.util.List;

/**
 * 应用服务
 * Created by yangyang115 on 18-7-27.
 */
public interface ApplicationService extends PageService<Application, QApplication> {

    /**
     * 查找应用
     *
     * @param code 应用代码
     * @return
     */
    Application findByCode(String code);

    /**
     * 查找应用
     *
     * @param codes 应用代码
     * @return
     */
    List<Application> findByCodes(List<String> codes);

    PageResult<TopicUnsubscribedApplication> findTopicUnsubscribedByQuery(QPageQuery<QApplication> query);

    PageResult<Application> findSubscribedByQuery(QPageQuery<QApplication> query);
}
