package nano.http.d2.hooks.impls;

import nano.http.d2.hooks.interfaces.ProxyHookProvider;

public class DefaultProxy implements ProxyHookProvider {
    @Override
    public boolean Accept(String uri) {
        return false;
    }
}
