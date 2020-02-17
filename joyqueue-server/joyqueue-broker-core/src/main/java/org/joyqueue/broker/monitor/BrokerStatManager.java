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
package org.joyqueue.broker.monitor;

import com.alibaba.fastjson.JSON;
import org.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import org.joyqueue.broker.monitor.converter.BrokerStatConverter;
import org.joyqueue.broker.monitor.exception.MonitorException;
import org.joyqueue.broker.monitor.model.BrokerStatPo;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.toolkit.io.DoubleCopy;
import org.joyqueue.toolkit.io.ZipUtil;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * brokerstat管理器
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class BrokerStatManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerStatManager.class);

    private static final int DEFAULT_FILE_SIZE = 1024 * 1024 * 100;

    private Integer brokerId;
    private BrokerMonitorConfig config;
    private BrokerStat brokerStat;
    private File statFile;
    private File newStatFile;

    private BrokerStatDoubleCopy brokerStatDoubleCopy;

    public BrokerStatManager(Integer brokerId, BrokerMonitorConfig config) {
        this.brokerId = brokerId;
        this.config = config;
        this.statFile = new File(config.getStatSaveFile());
        this.newStatFile = new File(config.getStatSaveFileNew());
        try {
            this.brokerStatDoubleCopy = new BrokerStatDoubleCopy(brokerId, config, newStatFile);
            this.brokerStat = recover();
        } catch (IOException e) {
            throw new MonitorException(e);
        }
    }

    protected BrokerStat recover() throws IOException {
        if (newStatFile.exists()) {
            this.brokerStatDoubleCopy.recover();
            BrokerStat brokerStat = this.brokerStatDoubleCopy.getBrokerStat();
            return brokerStat;
        } else {
            if (!statFile.exists()) {
                return new BrokerStat(brokerId);
            }

            String stat = FileUtils.readFileToString(statFile);

            if (StringUtils.isBlank(stat)) {
                return new BrokerStat(brokerId);
            }

            BrokerStatPo brokerStatPo = JSON.parseObject(stat, BrokerStatPo.class);

            if (brokerStatPo.getVersion() != BrokerStat.VERSION) {
                logger.warn("broker stat check version failed, current: {}, required: {}", brokerStatPo.getVersion(), BrokerStat.VERSION);
                return new BrokerStat(brokerId);
            }

            brokerStatPo.setBrokerId(brokerId);
            return BrokerStatConverter.convert(brokerStatPo);
        }
    }

    @Override
    protected void doStop() {
        brokerStatDoubleCopy.flush();
    }

    public BrokerStat getBrokerStat() {
        return this.brokerStat;
    }

    public void save() {
        brokerStatDoubleCopy.flush();
    }

    public static class BrokerStatDoubleCopy extends DoubleCopy {

        private Integer brokerId;
        private BrokerMonitorConfig config;
        private File statFile;

        private BrokerStat brokerStat;

        public BrokerStatDoubleCopy(Integer brokerId, BrokerMonitorConfig config, File statFile) throws IOException {
            super(statFile, DEFAULT_FILE_SIZE);
            this.brokerId = brokerId;
            this.config = config;
            this.statFile = statFile;
            this.brokerStat = new BrokerStat(brokerId);
        }

        @Override
        protected String getName() {
            return "BrokerStat";
        }

        @Override
        protected byte[] serialize() {
            try {
                BrokerStatPo brokerStatPo = BrokerStatConverter.convertToPo(brokerStat);
                byte[] stat = JSON.toJSONBytes(brokerStatPo);
                stat = ZipUtil.compress(stat);

                if (logger.isDebugEnabled()) {
                    logger.debug("save broker stat, value: {}, file: {}", new String(stat), statFile);
                }

                return stat;
            } catch (Exception e) {
                logger.error("serialize stat exception", e);
                return new byte[0];
            }
        }

        @Override
        protected void parse(byte[] data) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("load broker stat, value: {}, file: {}", new String(data), statFile);
                }

                data = ZipUtil.decompress(data).getBytes();
                BrokerStatPo brokerStatPo = JSON.parseObject(data, BrokerStatPo.class);

                if (brokerStatPo.getVersion() != BrokerStat.VERSION) {
                    logger.warn("broker stat check version failed, current: {}, required: {}", brokerStatPo.getVersion(), BrokerStat.VERSION);
                    brokerStat = new BrokerStat(brokerId);
                    return;
                }

                brokerStatPo.setBrokerId(brokerId);
                brokerStat = BrokerStatConverter.convert(brokerStatPo);
            } catch (Exception e) {
                logger.error("load broker stat exception, statFile: {}", statFile, e);
                throw new MonitorException("load broker stat exception", e);
            }
        }

        public BrokerStat getBrokerStat() {
            return brokerStat;
        }
    }
}