package nano.http.d2.hooks.impls;

import nano.http.d2.core.Response;
import nano.http.d2.hooks.interfaces.HeaderHookProvider;

public class DefaultHeader implements HeaderHookProvider {
    @Override
    public void process(Response r) {
        r.addHeader("Access-Control-Allow-Credentials", "true");
        r.addHeader("Access-Control-Allow-Headers", "*");
        r.addHeader("Access-Control-Allow-Methods", "*");
        r.addHeader("Access-Control-Allow-Origin", "*");
        r.addHeader("Access-Control-Max-Age", "10");
        r.addHeader("Access-Control-Expose-Headers", "*");
        r.addHeader("Connection", "close");
    }
}
