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
package com.jd.journalq.broker.monitor.convert;


import com.jd.journalq.broker.monitor.converter.Converter;
import com.jd.journalq.broker.monitor.converter.DefaultConverter;
import com.jd.journalq.broker.monitor.stat.BrokerStatExt;
import com.jd.journalq.model.MonitorRecord;
import com.jd.journalq.monitor.StringResponse;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @author lining11
 * Date: 2019/1/9
 */
public class PrometheusConvert implements Converter<BrokerStatExt, StringResponse> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusConvert.class);

    private DefaultConverter converter = new DefaultConverter();

    @Override
    public StringResponse convert(BrokerStatExt brokerStatExt) {

        List<MonitorRecord> records = converter.convert(brokerStatExt);

        StringResponse response = buildResponse(records);

        long time = brokerStatExt.getTimeStamp();
        if (time <= 0) {
            time = SystemClock.now() / 1000;
        }

        response.addHeader("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
        response.addHeader("Date", new Date(time * 1000).toString());
        response.addHeader("Transfer-Encoding", "chunked");

        logger.info("Report Prometheus Convert");

        return response;
    }

    private StringResponse buildResponse(List<MonitorRecord> records) {
        StringResponse response = new StringResponse();

        String body = buildPrometheusResult(records);
        response.setBody(body);

        return response;
    }

    private String buildPrometheusResult(List<MonitorRecord> records) {

        StringBuilder buffer = new StringBuilder();


        try {
            for (MonitorRecord mr : records) {

                buffer.append(mr.getMetric());
                buffer.append("{");

                for (int index = 1; index < 6; index++) {
                    String tag = mr.getTag("t" + index);
                    if (tag != null && tag.length() > 0) {
                        buffer.append("t" + index).append("=\"").append(tag).append("\"").append(",");
                    }
                }

                buffer.append("}");
                buffer.append(" ");
                buffer.append(mr.getValue() + "");
                buffer.append("\n");

            }
        } catch (Exception e) {
            logger.error("Convert metrics error!", e);
            return "\n";
        }

        return buffer.toString();
    }


    @Override
    public String type() {
        return "prometheus";
    }
}
