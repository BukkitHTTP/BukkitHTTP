package nano.http.d2.hooks.interfaces;

import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import java.util.Properties;

public interface RequestHookProvider {
    Response serve(String uri, String method, Properties header, Properties parms, Properties files, ServeProvider sp, String ip);
}
