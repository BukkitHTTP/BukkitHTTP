package nano.http.d2.ai;

import nano.http.d2.json.JSONArray;
import nano.http.d2.json.NanoJSON;

import java.util.function.Function;

public class Tool {
    public final String funcName;
    public final String funcDesc;
    public final String queryName;
    public final String queryDesc;
    public final Function<String, String> func;
     final NanoJSON desc = new NanoJSON();

    public Tool(String funcName, String funcDesc, String queryName, String queryDesc, Function<String, String> func) {
        this.funcName = funcName;
        this.funcDesc = funcDesc;
        this.queryName = queryName;
        this.queryDesc = queryDesc;
        this.func = func;
        desc.put("type", "function");

        NanoJSON prop = new NanoJSON();
        prop.put("name", funcName);
        prop.put("description", funcDesc);
        NanoJSON param = new NanoJSON();
        param.put("type", "object");
        NanoJSON paramProp = new NanoJSON();
        NanoJSON paramName = new NanoJSON();
        paramName.put("type", "string");
        paramName.put("description", queryDesc);
        paramProp.put(queryName, paramName);
        param.put("properties", paramProp);
        JSONArray req = new JSONArray();
        req.put(queryName);
        param.put("required", req);

        prop.put("parameters", param);
        desc.put("function", prop);
    }

    @Override
    public String toString() {
        return "Tool{" +
                "funcName='" + funcName + '\'' +
                ", funcDesc='" + funcDesc + '\'' +
                ", queryName='" + queryName + '\'' +
                ", queryDesc='" + queryDesc + '\'' +
                ", desc=" + desc +
                '}';
    }
}
