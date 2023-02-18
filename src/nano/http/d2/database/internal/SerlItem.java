package nano.http.d2.database.internal;

import java.io.Serializable;

class SerlItem implements Serializable {
    private static final long serialVersionUID = 11451419196666L;
    public Object[] key;
    public Object[] value;

    protected SerlItem(Object[] k, Object[] v) {
        key = k;
        value = v;
    }
}
