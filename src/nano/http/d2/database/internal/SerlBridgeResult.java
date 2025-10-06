package nano.http.d2.database.internal;

public class SerlBridgeResult {
    public final Object obj;
    public final boolean isDirty;

    SerlBridgeResult(Object obj, boolean isDirty) {
        this.obj = obj;
        this.isDirty = isDirty;
    }
}