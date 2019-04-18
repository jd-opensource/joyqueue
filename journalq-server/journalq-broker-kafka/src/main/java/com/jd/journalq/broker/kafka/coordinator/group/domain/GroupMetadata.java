package com.jd.journalq.broker.kafka.coordinator.group.domain;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.jd.journalq.broker.coordinator.group.domain.GroupMemberMetadata;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * GroupMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class GroupMetadata extends com.jd.journalq.broker.coordinator.group.domain.GroupMetadata {

    private GroupState state = GroupState.EMPTY;
    private int generationId = 0;
    private String leaderId;
    private String protocol;

    private String protocolType;

    private boolean newMemberAdded = false;
    private GroupState preState;
    private long preStateTimestamp;
    private Table<String, Integer, OffsetAndMetadata> offsetCache = HashBasedTable.create();

    private static final Map<GroupState, Set<GroupState>> ValidPreviousStates = Maps.newHashMap();

    static {
        ValidPreviousStates.put(GroupState.EMPTY,
                Sets.newHashSet(GroupState.PREPARINGREBALANCE));

        ValidPreviousStates.put(GroupState.DEAD,
                Sets.newHashSet(GroupState.EMPTY, GroupState.STABLE, GroupState.PREPARINGREBALANCE, GroupState.AWAITINGSYNC));

        ValidPreviousStates.put(GroupState.AWAITINGSYNC,
                Sets.newHashSet(GroupState.PREPARINGREBALANCE));

        ValidPreviousStates.put(GroupState.STABLE,
                Sets.newHashSet(GroupState.AWAITINGSYNC));

        ValidPreviousStates.put(GroupState.PREPARINGREBALANCE,
                Sets.newHashSet(GroupState.EMPTY, GroupState.STABLE, GroupState.AWAITINGSYNC));
    }

    public GroupMetadata(String groupId, String protocolType) {
        setId(groupId);
        this.protocolType = protocolType;
    }

    public GroupState getState() {
        return state;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getGenerationId() {
        return generationId;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isNewMemberAdded() {
        return newMemberAdded;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void setNewMemberAdded(boolean newMemberAdded) {
        this.newMemberAdded = newMemberAdded;
    }

    public GroupState getPreState() {
        return preState;
    }

    public long getPreStateTimestamp() {
        return preStateTimestamp;
    }

    public Map<String, Map<Integer, OffsetAndMetadata>> getOffsetCache() {
        return offsetCache.rowMap();
    }

    public OffsetAndMetadata getOffsetCache(String topic, int partition) {
        return offsetCache.get(topic, partition);
    }

    public void putOffsetCache(String topic, int partition, OffsetAndMetadata offsetAndMetadata) {
        offsetCache.put(topic, partition, offsetAndMetadata);
    }

    public OffsetAndMetadata removeOffsetCache(String topic, int partition) {
        return offsetCache.remove(topic, partition);
    }

    public void clearOffsetCache() {
        offsetCache.clear();
    }

    public boolean stateIs(GroupState groupState) {
        return state == groupState;
    }

    public boolean stateNot(GroupState groupState) {
        return state != groupState;
    }

    public boolean isHasMember(String memberId) {
        if (StringUtils.isBlank(memberId)) {
            return false;
        }
        return getMembers().containsKey(memberId);
    }

    public com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata getMember(String memberId) {
        if (StringUtils.isBlank(memberId)) {
            return null;
        }
        return (com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata) getMembers().get(memberId);
    }

    public boolean isNewGroup() {
        return generationId == 0;
    }

    public void addMember(com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata member) {
        if (StringUtils.isBlank(leaderId)) {
            leaderId = member.getId();
        }
        super.addMember(member);
    }

    public void removeMember(String memberId) {
        getMembers().remove(memberId);
        if (memberId.equals(leaderId)) {
            if (getMembers().isEmpty()) {
                leaderId = null;
            } else {
                Set<String> memberIds = getMembers().keySet();
                leaderId = memberIds.iterator().next();
            }
        }
    }

    public boolean isMemberEmpty() {
        return getMembers().isEmpty();
    }

    public List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> getNotYetRejoinedMembers() {
        List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> result = Lists.newLinkedList();
        for (Map.Entry<String, GroupMemberMetadata> entry : getMembers().entrySet()) {
            com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata member = (com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata) entry.getValue();
            if (member.getAwaitingJoinCallback() == null) {
                result.add(member);
            }
        }
        return result;
    }

    public List<String> getAllMemberIds() {
        return Lists.newArrayList(getMembers().keySet());
    }

    public List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> getAllMembers() {
        ConcurrentMap<String, GroupMemberMetadata> members = getMembers();
        if (MapUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> result = Lists.newArrayListWithExpectedSize(members.size());
        for (Map.Entry<String, GroupMemberMetadata> entry : members.entrySet()) {
            result.add((com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata) entry.getValue());
        }
        return result;
    }

    public int getMaxRebalanceTimeout() {
        if (MapUtils.isEmpty(getMembers())) {
            return 0;
        }
        int result = 0;
        for (Map.Entry<String, GroupMemberMetadata> entry : getMembers().entrySet()) {
            result = Math.max(((com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata) entry.getValue()).getRebalanceTimeoutMs(), result);
        }
        return result;
    }

    public boolean canRebalance() {
        return state == GroupState.EMPTY || state == GroupState.STABLE || state == GroupState.AWAITINGSYNC;
    }

    public void transitionStateTo(GroupState groupState) {
        assertValidTransition(groupState);
        preState = state;
        preStateTimestamp = SystemClock.now();
        state = groupState;
    }

    @Override
    public String getExtension() {
        return String.format("{state: '%s', preState: '%s', preStateTimestamp: %s, leader: '%s', generationId: '%s', protocol: '%s'}", state, preState, preStateTimestamp, leaderId, generationId, protocol);
    }

    public String selectProtocol() {
        if (getMembers().isEmpty()) {
            throw new IllegalStateException("Cannot select protocol for empty group");
        }

        // select the protocol for this group which is supported by all members
        List<String> candidates = candidateProtocols();
        Map<String, Integer> mapValues = Maps.newHashMap();

        // let each member vote for one of the protocols and choose the one with the most votes
        List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> allMemberMetadata = getAllMembers();
        for (com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata memberMetadata : allMemberMetadata) {
            String protocol = memberMetadata.vote(candidates);
            Integer value = mapValues.get(protocol);
            if (value == null) {
                mapValues.put(protocol, 1);
            } else {
                value++;
                mapValues.put(protocol, value);
            }
        }
        String selectProtocol = null;
        int max = 0;
        Iterator<Map.Entry<String, Integer>> iter = mapValues.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = iter.next();
            String protocol = entry.getKey();
            int size = entry.getValue();
            if (max < size) {
                selectProtocol = protocol;
            }
        }
        return selectProtocol;
    }

    private List<String> candidateProtocols() {
        // get the set of protocols that are commonly supported by all members
        List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> allMemberMetadata = getAllMembers();
        List<String> commonProtocols = Lists.newLinkedList();
        List<Set<String>> allMemberProtocol = Lists.newLinkedList();
        if (allMemberMetadata != null) {
            for (com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata memberMetadata : allMemberMetadata) {
                Set<String> protocols = memberMetadata.protocols();
                allMemberProtocol.add(protocols);
            }
        }
        for (int i = 0; i < allMemberProtocol.size(); i++) {
            if (i == 0) {
                commonProtocols.addAll(allMemberProtocol.get(i));
            } else {
                commonProtocols.retainAll(allMemberProtocol.get(i));
            }
        }

        return commonProtocols;
    }

    public boolean supportsProtocols(Set<String> memberProtocols) {
        Set<String> result = Sets.newHashSet();
        result.addAll(memberProtocols);
        result.retainAll(candidateProtocols());
        return isMemberEmpty() || !result.isEmpty();
    }

    public boolean initNextGeneration() {
        List<com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata> memberMetadataSet = getNotYetRejoinedMembers();
        if (CollectionUtils.isNotEmpty(memberMetadataSet)) {
            return false;
        }
        generationId += 1;
        protocol = selectProtocol();
        transitionStateTo(GroupState.AWAITINGSYNC);
        return true;
    }

    public Map<String, byte[]> currentMemberMetadata() {
        if (stateIs(GroupState.DEAD) || stateIs(GroupState.PREPARINGREBALANCE)) {
            throw new IllegalStateException("Cannot obtain member metadata for group in state " + state.toString());
        }
        Map<String, byte[]> memeberMeta = Maps.newHashMap();
        Iterator<Map.Entry<String, GroupMemberMetadata>> iterator = getMembers().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, GroupMemberMetadata> entry = iterator.next();
            String memberId = entry.getKey();
            com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata memberMetadata = (com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMemberMetadata) entry.getValue();
            byte[] metadata = memberMetadata.metadata(protocol);
            memeberMeta.put(memberId, metadata);
        }
        return memeberMeta;
    }

    private void assertValidTransition(GroupState targetState) {
        if (!com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata.ValidPreviousStates.get(targetState).contains(state)) {
            throw new IllegalStateException(String.format("Group %s should be in the %s states before moving to" +
                            " %s state. Instead it is in %s state",
                    getId(), com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata.ValidPreviousStates.get(targetState), targetState, state));
        }
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s,%s]", getId(), protocolType, getState(), getMembers());
    }

}
