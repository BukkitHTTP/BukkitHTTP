package nano.http.d2.ai;

public class StreamingResult {
    public final StringBuilder text = new StringBuilder();
    public final StringBuilder reasoning = new StringBuilder();
    boolean isFinished = false;
    boolean isError = false;
    String delta = null;

    public String getDelta() {
        return delta;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isError() {
        return isError;
    }
}
