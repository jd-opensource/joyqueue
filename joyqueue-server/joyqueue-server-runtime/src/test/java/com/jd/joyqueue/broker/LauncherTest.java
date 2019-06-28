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
package com.jd.joyqueue.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chengzhiliang on 2018/9/27.
 */
public class LauncherTest {

    protected static final Logger logger = LoggerFactory.getLogger(LauncherTest.class);

    public static void main(String[] args) throws Exception {
        BrokerServiceTest brokerService = new BrokerServiceTest();
        try {
            brokerService.start();
            logger.info("JoyQueue Test is start");
        } catch (Throwable t) {
            logger.error("JoyQueue Test start exception", t);
            brokerService.stop();
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    brokerService.stop();
                } catch (Throwable t) {
                    logger.error("JoyQueue Test stop exception", t);
                    System.exit(-1);
                }
            }
        });
    }
}