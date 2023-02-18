package nano.http.d2.hooks.interfaces;

import nano.http.d2.core.Response;

public interface HeaderHookProvider {
    void process(Response r);
}
