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
package org.joyqueue.network.transport.codec;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * JoyQueueHeader
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class JoyQueueHeader implements Header {

    public static final byte VERSION_V1 = 1;
    public static final byte VERSION_V2 = 2;
    public static final byte VERSION_V3 = 3;
    public static final byte VERSION_V4 = 4;

    public static final byte CURRENT_VERSION = VERSION_V4;

    public static final int MAGIC = 0xCAFEBEBE;

    private static final AtomicInteger requestIdGenerator = new AtomicInteger(0);

    // 版本
    private byte version = CURRENT_VERSION;
    // ack
    private QosLevel qosLevel;
    // 方向
    private Direction direction;
    // 请求id
    private int requestId;
    // 命令类型
    private int type;
    //请求或响应时间
    private long time;

    // 响应部分
    // 状态码
    private short status = (short) JoyQueueCode.SUCCESS.getCode();
    // 错误信息
    private String error;

    public JoyQueueHeader() {
    }

    public JoyQueueHeader(int type) {
        this(Direction.REQUEST, type);
    }

    public JoyQueueHeader(Direction direction, int type) {
        this(direction, QosLevel.RECEIVE, type);
    }

    public JoyQueueHeader(Direction direction, QosLevel qosLevel, int type) {
        this.type = type;
        this.version = CURRENT_VERSION;
        this.direction = direction;
        this.qosLevel = qosLevel;
        this.time = SystemClock.now();
        this.requestId = generateRequestId();
    }

    public JoyQueueHeader(byte version, QosLevel qosLevel, Direction direction, int requestId, int type, long time, short status, String error) {
        this.version = version;
        this.qosLevel = qosLevel;
        this.direction = direction;
        this.requestId = requestId;
        this.type = type;
        this.time = time;
        this.status = status;
        this.error = error;
    }

    @Override
    public int getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = (byte) version;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setQosLevel(QosLevel qosLevel) {
        this.qosLevel = qosLevel;
    }

    @Override
    public QosLevel getQosLevel() {
        return qosLevel;
    }

    public long getTime() {
        return time;
    }

    @Override
    public void setStatus(int status) {
        this.status = (short) status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String getError() {
        return error;
    }

    protected int generateRequestId() {
        int id = requestIdGenerator.incrementAndGet();
        if (id > 0) {
            return id;
        }
        if (requestIdGenerator.compareAndSet(id, 0)) {
            return 0;
        } else {
            return generateRequestId();
        }
    }

    @Override
    public String toString() {
        return "JoyQueueHeader{" +
                "version=" + version +
                ", qosLevel=" + qosLevel +
                ", requestId=" + requestId +
                ", type=" + type +
                ", time=" + time +
                ", status=" + status +
                ", error='" + error + '\'' +
                '}';
    }
}