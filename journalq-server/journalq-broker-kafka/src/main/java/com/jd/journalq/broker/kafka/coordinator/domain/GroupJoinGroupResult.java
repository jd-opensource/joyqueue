package com.jd.journalq.broker.kafka.coordinator.domain;

import java.util.Collections;
import java.util.Map;

public class GroupJoinGroupResult {

    public static final int NONE_GENERATION_ID = 0;
    public static final String NO_PROTOCOL = "";
    public static final String NO_LEADER = "";

    private Map<String, byte[]> members;
    private String memberId;
    private int generationId;
    private String subProtocol;
    private String leaderId;
    private short errorCode;

    public GroupJoinGroupResult(Map<String, byte[]> members, String memberId, int generationId,
                                String subProtocol, String leaderId, short errorCode) {
        this.members = members;
        this.memberId = memberId;
        this.generationId = generationId;
        this.subProtocol = subProtocol;
        this.leaderId = leaderId;
        this.errorCode = errorCode;
    }

    public static GroupJoinGroupResult buildError(String memberId, short errorCode) {
        return new GroupJoinGroupResult(Collections.emptyMap(), memberId, NONE_GENERATION_ID, NO_PROTOCOL, NO_LEADER, errorCode);
    }

    public Map<String, byte[]> getMembers() {
        return members;
    }

    public String getMemberId() {
        return memberId;
    }

    public int getGenerationId() {
        return generationId;
    }

    public String getSubProtocol() {
        return subProtocol;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public short getErrorCode() {
        return errorCode;
    }
}