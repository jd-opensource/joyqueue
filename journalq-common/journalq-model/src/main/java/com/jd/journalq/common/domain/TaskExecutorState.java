package com.jd.journalq.common.domain;


import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 用于监控 task executor，
 *
 * 添加upm存活报警
 *
 **/
public class TaskExecutorState implements Serializable {


    /**
     * 启动时间
     *
     **/
    private long bootstrapTime;

    /**
     *
     **/
    private volatile boolean leader;

    /**
     * 正在执行的任务id
     **/
    private volatile Set<Long> taskIds;
    /**
     * 如果是主，则有效
     *
     **/
    private volatile long lastDispatchSuccessTime;


    /**
     * 如果是主，则有效
     * 上次分发的任务数量
     **/
    private volatile long lastDispatchTaskAmount;
    /**
     *
     * 最后一个任务的开始时间
     *
     **/
    private volatile long lastTaskStartTime;

    /**
     *  时间间隔内,平均任务执行时间,不包括等待时间
     *
     **/
    private volatile long taskExecuteTime;

    /**
     * 时间间隔内，执行的任务总数量
     **/
    private AtomicInteger taskExecuteAmount=new AtomicInteger(0);

    private long  avgTaskExecuteTime;



    /**
     *
     * jvm state
     *
     **/


    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public Set<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(Set<Long> taskIds) {
        this.taskIds = taskIds;
    }

    public long getLastDispatchSuccessTime() {
        return lastDispatchSuccessTime;
    }

    public void setLastDispatchSuccessTime(long lastDispatchSuccessTime) {
        this.lastDispatchSuccessTime = lastDispatchSuccessTime;
    }

    public long getLastTaskStartTime() {
        return lastTaskStartTime;
    }

    public void setLastTaskStartTime(long lastTaskStartTime) {
        this.lastTaskStartTime = lastTaskStartTime;
    }


    public long getTaskExecuteTime() {
        return taskExecuteTime;
    }

    public void setTaskExecuteTime(long taskExecuteTime) {
        this.taskExecuteTime = taskExecuteTime;
    }


    public AtomicInteger getTaskExecuteAmount() {
        return taskExecuteAmount;
    }

    public void setTaskExecuteAmount(AtomicInteger taskExecuteAmount) {
        this.taskExecuteAmount = taskExecuteAmount;
    }


    public long getBootstrapTime() {
        return bootstrapTime;
    }

    public void setBootstrapTime(long bootstrapTime) {
        this.bootstrapTime = bootstrapTime;
    }

    public long getLastDispatchTaskAmount() {
        return lastDispatchTaskAmount;
    }

    public void setLastDispatchTaskAmount(long lastDispatchTaskAmount) {
        this.lastDispatchTaskAmount = lastDispatchTaskAmount;
    }

    public long getAvgTaskExecuteTime() {
        if(taskExecuteAmount.get()!=0){
            avgTaskExecuteTime=  taskExecuteTime/taskExecuteAmount.get();
        }
        return avgTaskExecuteTime;
    }
}
