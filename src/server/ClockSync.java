package server;

public class ClockSync {
    private int logicalClock;

    public ClockSync() {
        this.logicalClock = 0;
    }

    public synchronized void increment() {
        logicalClock++;
    }

    public synchronized int getLogicalClock() {
        return logicalClock;
    }
}