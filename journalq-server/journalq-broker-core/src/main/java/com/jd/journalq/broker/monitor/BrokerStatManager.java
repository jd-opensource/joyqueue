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
package com.jd.journalq.broker.monitor;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.broker.monitor.config.BrokerMonitorConfig;
import com.jd.journalq.broker.monitor.converter.BrokerStatConverter;
import com.jd.journalq.broker.monitor.exception.MonitorException;
import com.jd.journalq.broker.monitor.model.BrokerStatPo;
import com.jd.journalq.broker.monitor.stat.BrokerStat;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * brokerstat管理器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
// TODO 定期清理
public class BrokerStatManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerStatManager.class);

    private Integer brokerId;
    private BrokerMonitorConfig config;
    private BrokerStat brokerStat;
    private File statFile;

    public BrokerStatManager(Integer brokerId, BrokerMonitorConfig config) {
        this.brokerId = brokerId;
        this.config = config;
        this.statFile = new File(config.getStatSaveFile());
        this.brokerStat = load();
    }

    @Override
    protected void doStop() {
        save();
    }

    public BrokerStat getBrokerStat() {
        return this.brokerStat;
    }

    public BrokerStat load() {
        try {
            if (!statFile.exists()) {
                return new BrokerStat(brokerId);
            }

            String stat = FileUtils.readFileToString(statFile);

            if (logger.isDebugEnabled()) {
                logger.debug("load broker stat, value: {}, file: {}", stat, statFile);
            }

            if (StringUtils.isBlank(stat)) {
                logger.warn("load broker stat error, value: {}, file: {}", stat, statFile);
                return new BrokerStat(brokerId);
            }

            BrokerStatPo brokerStatPo = JSON.parseObject(stat, BrokerStatPo.class);

            if (brokerStatPo.getVersion() != BrokerStat.VERSION) {
                logger.warn("broker stat check version failed, current: {}, required: {}", brokerStatPo.getVersion(), BrokerStat.VERSION);
                return new BrokerStat(brokerId);
            }

            brokerStatPo.setBrokerId(brokerId);
            return BrokerStatConverter.convert(brokerStatPo);
        } catch (Exception e) {
            logger.error("load broker stat exception, statFile: {}", statFile, e);
            throw new MonitorException("load broker stat exception", e);
        }
    }

    public void save() {
        try {
            BrokerStatPo brokerStatPo = BrokerStatConverter.convertToPo(brokerStat);
            String stat = JSON.toJSONString(brokerStatPo);

            if (logger.isDebugEnabled()) {
                logger.debug("save broker stat, value: {}, file: {}", stat, statFile);
            }

            FileUtils.write(statFile, stat);
        } catch (IOException e) {
            logger.error("save broker stat exception, statFile: {}", statFile, e);
        }
    }
}