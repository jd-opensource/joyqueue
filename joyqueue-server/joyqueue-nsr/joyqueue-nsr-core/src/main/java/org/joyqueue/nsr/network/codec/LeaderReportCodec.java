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
package org.joyqueue.nsr.network.codec;

import org.joyqueue.domain.TopicName;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.LeaderReport;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.Set;
import java.util.TreeSet;


/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class LeaderReportCodec implements NsrPayloadCodec<LeaderReport>, Type {
    @Override
    public LeaderReport decode(Header header, ByteBuf buffer) throws Exception {
        LeaderReport leaderReport = new LeaderReport();
        leaderReport.topic(TopicName.parse(Serializer.readString(buffer)));
        leaderReport.partitionGroup(buffer.readInt());
        leaderReport.leaderBrokerId(buffer.readInt());
        leaderReport.termId(buffer.readInt());
        boolean hasIsr = buffer.readBoolean();
        if(hasIsr){
            int isrSize = buffer.readInt();
            Set<Integer> isrs = new TreeSet();
            for (int i = 0; i < isrSize; i++) {
                isrs.add(buffer.readInt());
            }
            leaderReport.isrId(isrs);
        }
        return leaderReport;
    }

    @Override
    public void encode(LeaderReport payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic().getFullName(),buffer);
        buffer.writeInt(payload.getPartitionGroup());
        buffer.writeInt(payload.getLeaderBrokerId());
        buffer.writeInt(payload.getTermId());
        if (null == payload.getIsrId()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeInt(payload.getIsrId().size());
            for (Integer isr : payload.getIsrId()) {
                buffer.writeInt(isr);
            }
        }
    }

    @Override
    public int type() {
        return NsrCommandType.LEADER_REPORT;
    }
}
