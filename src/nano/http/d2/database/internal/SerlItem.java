package nano.http.d2.database.internal;

import java.io.Serializable;

class SerlItem implements Serializable {
    private static final long serialVersionUID = 11451419196666L;
    public final Object[] key;
    public final Object[] value;

    protected SerlItem(Object[] k, Object[] v) {
        key = k;
        value = v;
    }
}
