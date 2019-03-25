package com.jd.journalq.repository;

import com.jd.journalq.model.domain.Application;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.common.model.QPageQuery;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 应用 仓库
 * Created by chenyanying3 on 2018-10-15
 */
@Repository
public interface ApplicationRepository extends PageRepository<Application, QApplication> {

    /**
     * 查询未订阅的app
     * @param query
     * @return
     */
    PageResult<Application> findUnsubscribedByQuery(QPageQuery<QApplication> query);

    /**
     * 根据代码查找
     *
     * @param applicationCode
     * @return
     */
    Application findByCode(String applicationCode);


    /**
     * 根据代码查找
     *
     * @param codes
     * @return
     */
    List<Application> findByCodes(List<String> codes);

//    /**
//     * 根据代码查找,检索出已删除应用
//     *
//     * @param applicationCode
//     * @return
//     */
//    List<Application> findWithDeletedByCode(String applicationCode);

}
