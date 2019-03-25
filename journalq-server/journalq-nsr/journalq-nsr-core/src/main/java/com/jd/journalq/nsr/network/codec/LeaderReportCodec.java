package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.LeaderReport;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
