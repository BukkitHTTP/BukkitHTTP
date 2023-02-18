package nano.http.d2.hooks.interfaces;

public interface FileHookProvider {
    boolean Accept(String filename, String uri);
}
