package com.jd.journalq.broker.network.traffic;

import com.jd.journalq.network.transport.command.Payload;

/**
 * TrafficCommand
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public interface TrafficPayload extends Payload {

    Traffic getTraffic();
}