package nano.http.d2.utils;

public class RateLimit {
    private final long maxDelta;
    private final long add;
    private final Object lock = new Object();
    private long time;

    public RateLimit(int maxBurstTokens, int secondsPerToken) {
        this.time = Long.MIN_VALUE;
        this.add = 1000L * secondsPerToken;
        this.maxDelta = this.add * maxBurstTokens;
    }

    public boolean tryPoll(int d) {
        synchronized (lock) {
            long now = System.currentTimeMillis();
            this.time = Math.max(this.time, now - maxDelta);
            long pending = this.time + this.add * d;
            if (pending > now) {
                return false;
            } else {
                this.time = pending;
                return true;
            }
        }
    }

    public void banFor(int seconds) {
        synchronized (lock) {
            this.time = System.currentTimeMillis() + seconds * 1000L;
        }
    }

    public void reset() {
        synchronized (lock) {
            this.time = Long.MIN_VALUE;
        }
    }
}
