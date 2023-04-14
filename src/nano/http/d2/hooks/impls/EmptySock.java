package nano.http.d2.hooks.impls;

import nano.http.d2.hooks.interfaces.SocketHookProvider;

public class EmptySock implements SocketHookProvider {
    @Override
    public boolean Accept(String ip) {
        return true;
    }
}
