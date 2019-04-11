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

import com.jd.journalq.service.BrokerRestUrlMappingService;
import com.jd.journalq.service.impl.BrokerRestUrlMappingServiceImpl;
import org.junit.Test;

public class UrlMappingTest {


    @Test
    public void urlTest(){
        BrokerRestUrlMappingService urlMappingService=new BrokerRestUrlMappingServiceImpl();
        String url=urlMappingService.urlTemplate("appClientMonitor");
        System.out.println(url);

    }
}
