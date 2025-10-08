package nano.http.d2.ai.mcp;

import nano.http.d2.ai.Tool;

import java.util.List;

public class MCPToolList {
    public final SSERPC rpc;
    public final List<Tool> tools;

    public MCPToolList(SSERPC rpc, List<Tool> tools) {
        this.rpc = rpc;
        this.tools = tools;
    }
}
