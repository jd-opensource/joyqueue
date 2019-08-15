package io.chubao.joyqueue.broker.network.traffic;

import io.chubao.joyqueue.network.transport.command.Payload;

/**
 * TrafficCommand
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public interface TrafficPayload extends Payload {

    Traffic getTraffic();
}