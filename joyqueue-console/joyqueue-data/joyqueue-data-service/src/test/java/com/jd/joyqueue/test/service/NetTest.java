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
package com.jd.joyqueue.test.service;

import com.jd.joyqueue.model.domain.BrokerMonitorRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class NetTest {
    Logger logger= LoggerFactory.getLogger(NetTest.class);

    @Test
    public void test(){
        logger.info("aa");

    }

    @Test
    public void lamdaSort(){

        Random random=new Random();
        List<BrokerMonitorRecord> recordList=new ArrayList();
        BrokerMonitorRecord record;
        for(int i=0;i<100;i++){
            record=new BrokerMonitorRecord();
            record.setPartition(random.nextInt(100));
            recordList.add(record);
        }

        recordList.sort(Comparator.comparing(e->e.getPartition()));
        System.out.println();

    }
}
