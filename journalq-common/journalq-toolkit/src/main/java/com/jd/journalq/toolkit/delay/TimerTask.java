package com.jd.journalq.toolkit.delay;

public abstract class TimerTask implements Runnable {
    protected long delayMs;  // timestamp in millisecond

    protected TimerTaskList.TimerTaskEntry timerTaskEntry = null;

    public synchronized void cancel() {
        if (timerTaskEntry != null) {
            timerTaskEntry.remove();
        }
        timerTaskEntry = null;
    }

    protected synchronized void setTimerTaskEntry(TimerTaskList.TimerTaskEntry entry) {
        // if this timerTask is already held by an existing timer task entry,
        // we will remove such an entry first.
        if (timerTaskEntry != null && !timerTaskEntry.equals(entry)) {
            timerTaskEntry.remove();
        }
        timerTaskEntry = entry;
    }

    protected TimerTaskList.TimerTaskEntry getTimerTaskEntry() {
        return timerTaskEntry;
    }
}

