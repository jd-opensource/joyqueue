package io.chubao.joyqueue.toolkit.network;

import java.util.List;

/**
 * 网络拓扑图
 * Created by hexiaofeng on 16-7-6.
 */
public class Topology<T extends Lan> {
    // 数据中心
    private List<T> lans;
    // 专线
    private List<Line<T>> lines;

    public Topology(List<T> lans, List<Line<T>> lines) {
        this.lans = lans;
        this.lines = lines;
    }

    public List<T> getLans() {
        return lans;
    }

    public List<Line<T>> getLines() {
        return lines;
    }
}
