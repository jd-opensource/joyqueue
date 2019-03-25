package com.jd.journalq.test.service;

import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.model.Pagination;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.service.ApplicationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by yangyang115 on 18-7-27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-config.xml"})
public class ApplicationServiceTest {

    @Autowired
    private ApplicationService service;

    @Test
    public void findByIdTest(){
        System.out.println(service.findById(1));
    }

    @Test
    public void findByQueryTest(){
        QPageQuery<QApplication> query = new QPageQuery<>(new Pagination(0), new QApplication("test", null));
        System.out.println(service.findByQuery(query));
    }
}
