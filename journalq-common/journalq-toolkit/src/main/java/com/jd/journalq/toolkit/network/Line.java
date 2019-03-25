package com.jd.journalq.toolkit.network;

/**
 * 专线
 * Created by hexiaofeng on 16-7-6.
 */
public class Line<T extends Lan> {
    // 机房1
    private T from;
    // 机房2
    private T to;
    // 丢包率
    private double loss;
    // 延迟率
    private double rtt;
    // 入口流量占比
    private double in;
    // 出口流量占比
    private double out;

    public Line() {
    }

    public Line(T from, T to) {
        this.from = from;
        this.to = to;
    }

    public Line(T from, T to, double loss, double rtt, double in, double out) {
        this.from = from;
        this.to = to;
        this.loss = loss;
        this.rtt = rtt;
        this.in = in;
        this.out = out;
    }

    public T getFrom() {
        return from;
    }

    public void setFrom(T from) {
        this.from = from;
    }

    public T getTo() {
        return to;
    }

    public void setTo(T to) {
        this.to = to;
    }

    public double getLoss() {
        return loss;
    }

    public void setLoss(double loss) {
        this.loss = loss;
    }

    public double getRtt() {
        return rtt;
    }

    public void setRtt(double rtt) {
        this.rtt = rtt;
    }

    public double getIn() {
        return in;
    }

    public void setIn(double in) {
        this.in = in;
    }

    public double getOut() {
        return out;
    }

    public void setOut(double out) {
        this.out = out;
    }
}
