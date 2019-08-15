package io.chubao.joyqueue.broker.election;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/11
 */
public interface ElectionNode {
    int INVALID_NODE_ID = -1;

    /**
     * 获取节点当前状态
     * @return 节点状态
     */
    State getState();

    /**
     * 设置选举节点状态
     * @param state 节点状态
     */
    void setState(State state);

    /**
     * 获取节点地址信息
     * @return 节点地址
     */
    String getAddress();

    /**
     * 设置节点优先级，对于raft可能是watermark
     * @param priority 节点优先级
     */
    void setPriority(long priority);

    /**
     * 获取节点优先级
     * @return 节点优先级
     */
    long getPriority();

    /**
     * 获取节点ID
     * @return 节点Id
     */
    int getNodeId();

    /**
     * 是否获得选票
     * @return 是否投票
     */
    boolean isVoteGranted();

    /**
     * 设置获取选票标记
     * @param voteGranted 是否投票
     */
    void setVoteGranted(boolean voteGranted);

    /**
     * 节点是否相同
     * @param node
     * @return 节点是否相同
     */
    boolean equals(ElectionNode node);

    enum State {
        LEADER,
        TRANSFERRING,
        FOLLOWER,
        CONDIDATE
    }
}
