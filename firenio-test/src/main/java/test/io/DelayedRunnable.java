package test.io;

/**
 * @author: wangkai
 **/
public abstract class DelayedRunnable implements Runnable {

    static final long CANCEL_MASK = 1L << 63;
    static final long DONE_MASK   = 1L << 62;
    static final long DELAY_MASK  = ~(CANCEL_MASK | DONE_MASK);

    private long scheduleTime;

    private DelayedRunnable next;

    public DelayedRunnable(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public long getScheduleTime() {
        return scheduleTime;
    }

    public DelayedRunnable getNext() {
        return next;
    }

    public void setNext(DelayedRunnable next) {
        this.next = next;
    }

    public void cancel() {
        this.scheduleTime |= CANCEL_MASK;
    }

    public long getDelay() {
        return (this.scheduleTime & DELAY_MASK);
    }

    public boolean isCanceled() {
        return (this.scheduleTime & CANCEL_MASK) != 0;
    }

    public boolean isDone() {
        return (this.scheduleTime & DONE_MASK) != 0;
    }

    public void done() {
        this.scheduleTime |= DONE_MASK;
    }

}
