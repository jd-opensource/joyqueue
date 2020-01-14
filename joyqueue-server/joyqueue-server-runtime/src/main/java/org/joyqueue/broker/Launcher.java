/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher
 */
public class Launcher {

    protected static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        BrokerService brokerService = new BrokerService(args);

        try {
            brokerService.start();
            BannerPrinter.print();
            logger.info("JoyQueue is started");
        } catch (Throwable t) {
            logger.error("JoyQueue start exception", t);
            brokerService.stop();
            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                brokerService.stop();
                logger.info("JoyQueue stopped");
            } catch (Throwable t) {
                logger.error("JoyQueue stop exception", t);
                System.exit(-1);
            }
        }));
    }
}