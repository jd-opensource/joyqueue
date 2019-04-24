/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
