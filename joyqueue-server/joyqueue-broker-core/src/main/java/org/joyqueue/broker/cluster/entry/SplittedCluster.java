package org.joyqueue.broker.cluster.entry;

import java.util.List;
import java.util.Map;

public class SplittedCluster {

    private boolean isLocal;
    private Map<Integer /** brokerId **/, List<Integer /** group **/>> splittedByGroup;
    private Map<Integer /** leaderId **/, List<Integer /** group **/>> splittedByLeader;

    public SplittedCluster() {

    }

    public SplittedCluster(boolean isLocal, Map<Integer, List<Integer>> splittedByGroup, Map<Integer, List<Integer>> splittedByLeader) {
        this.isLocal = isLocal;
        this.splittedByGroup = splittedByGroup;
        this.splittedByLeader = splittedByLeader;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public Map<Integer, List<Integer>> getSplittedByGroup() {
        return splittedByGroup;
    }

    public void setSplittedByGroup(Map<Integer, List<Integer>> splittedByGroup) {
        this.splittedByGroup = splittedByGroup;
    }

    public Map<Integer, List<Integer>> getSplittedByLeader() {
        return splittedByLeader;
    }

    public void setSplittedByLeader(Map<Integer, List<Integer>> splittedByLeader) {
        this.splittedByLeader = splittedByLeader;
    }
}