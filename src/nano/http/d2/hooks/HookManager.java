package nano.http.d2.hooks;

import nano.http.d2.hooks.impls.*;
import nano.http.d2.hooks.interfaces.*;

/**
 * Important! Use this class if and only if you know what you're doing.
 * We do not provide any support if you've modified any field of this class.
 * While using this class grants you the ability to modify the behavior of NanoHTTPd2,
 * it can also be dangerous if you make a mistake here.
 * Also, we do not guarantee that this class will be unchanged in future versions.
 * So, it's recommended that you copy&paste the SOURCE of NanoHTTPd2 into your project.
 *
 * @author HsTeam
 */

public class HookManager {
    public static RequestHookProvider requestHook = new DefaultRequest();
    // This hook is called before the request is processed by the ServeProvider.
    // It can be used to authenticate the request, or work as a WAF.

    public static HeaderHookProvider headerHook = new DefaultHeader();
    // This hook is called when a response is just created.
    // It can be used to add headers to the response.

    public static SocketHookProvider socketHook = new DefaultSock();
    // This hook is called when a socket is just created.
    // It can be used to work as a firewall, as the default implementation is.

    public static FileHookProvider fileHook = new DefaultFile();
    // This hook is called when a file is about to be uploaded.
    // By default, we allow files to be uploaded only if the uri ends with upload.html.
    // You may also make use of this hook to disable file uploads.

    public static void invoke() {
    }
    // This method is used to make sure that the class is loaded.
}
