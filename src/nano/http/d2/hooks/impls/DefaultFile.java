package nano.http.d2.hooks.impls;

import nano.http.d2.hooks.interfaces.FileHookProvider;

public class DefaultFile implements FileHookProvider {
    @Override
    public boolean Accept(String filename, String uri) {
        return uri.endsWith("upload.html");
    }
}
