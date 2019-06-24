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

import java.io.InputStream;
import java.util.Properties;

/**
 * joyqueue加载器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/27
 */
public class Launcher {
    protected static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    protected static final String SYSTEM_EVN_FILE = "system.properties";
    protected static final String separator = "\n";

    public static void main(String[] args) throws Exception {
        BrokerService brokerService = new BrokerService(args);
        try {
            Properties sysEvn = new Properties();
            InputStream inputStream = Launcher.class.getClassLoader().getResourceAsStream(SYSTEM_EVN_FILE);
            if (null != inputStream) {
                sysEvn.load(inputStream);
            }
            sysEvn.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
            brokerService.start();
            logger.info(
                    ">>>" + separator +
                            ">>>       _   __  __    ____  " + separator +
                            ">>>      | | |  \\/  |  / __ \\ " + separator +
                            ">>>      | | | \\  / | | |  | |" + separator +
                            ">>>  _   | | | |\\/| | | |  | |" + separator +
                            ">>> | |__| | | |  | | | |__| |" + separator +
                            ">>> \\______/ |_|  |_| \\__\\__\\/" + separator +
                            ">>>                           ");
            logger.info("JoyQueue is started");
        } catch (Throwable t) {
            logger.error("JoyQueue start exception", t);
            brokerService.stop();
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    brokerService.stop();
                    logger.info("JoyQueue stopped");
                } catch (Throwable t) {
                    logger.error("JoyQueue stop exception", t);
                    System.exit(-1);
                }
            }
        });
    }
}