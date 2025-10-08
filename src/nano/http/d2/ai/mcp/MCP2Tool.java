package nano.http.d2.ai.mcp;

import nano.http.d2.ai.OpenRouter;
import nano.http.d2.ai.Tool;
import nano.http.d2.ai.data.ChatContext;
import nano.http.d2.ai.data.Message;
import nano.http.d2.json.JSONArray;
import nano.http.d2.json.NanoJSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MCP2Tool {
    public static MCPToolList fromMcp(String MCPAddr, boolean strict) throws IOException {
        SSERPC rpc = new SSERPC(MCPAddr);
        try {
            NanoJSON init = new NanoJSON();
            init.put("protocolVersion", "1.0");
            NanoJSON clientInfo = new NanoJSON();
            clientInfo.put("name", "BukkitHTTP - MCP Client");
            clientInfo.put("version", "v251009-1");
            init.put("clientInfo", clientInfo);
            NanoJSON capabilities = new NanoJSON();
            NanoJSON tools = new NanoJSON();
            tools.put("listChanged", false);
            capabilities.put("tools", tools);
            init.put("capabilities", capabilities);
            rpc.sendJsonRPCAndWaitForResp("initialize", init);
            rpc.sendJsonRPCUnsafe("notifications/initialized", null, false);

            NanoJSON c = new NanoJSON();
            NanoJSON resp = rpc.sendJsonRPCAndWaitForResp("tools/list", c);
            JSONArray arr = resp.getJSONArray("tools");
            if (arr.isEmpty()) {
                throw new IOException("No tools available from MCP");
            }

            List<Tool> toolList = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                NanoJSON tool = arr.getJSONObject(i);


                String funcName = tool.getString("name");
                String funcDesc = tool.getString("description");
                tool = tool.getJSONObject("inputSchema").getJSONObject("properties");
                Set<String> parms = tool.keySet();

                String parmName = null;
                for (String k : parms) {
                    NanoJSON p = tool.getJSONObject(k);
                    if (p.has("default") && p.length() != 1) {
                        continue;
                    }
                    if (parmName != null && strict) {
                        throw new IOException("MCP Tool has multiple parameters, cannot determine which to use in strict mode: " + funcName);
                    }
                    parmName = k;
                }
                NanoJSON parm = tool.getJSONObject(parmName);
                String parmDesc = parm.getString("description");
                String type = parm.getString("type");

                String finalParmName = parmName;
                Tool t = new Tool(funcName, funcDesc, parmName, parmDesc, (s) -> {
                    try {
                        NanoJSON callParms = new NanoJSON();
                        callParms.put("name", funcName);
                        NanoJSON args = new NanoJSON();
                        Object v = asType(type, s);
                        args.put(finalParmName, v);
                        callParms.put("arguments", args);
                        NanoJSON call = rpc.sendJsonRPCAndWaitForResp("tools/call", callParms);
                        JSONArray content = call.getJSONArray("content");
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < content.length(); j++) {
                            NanoJSON part = content.getJSONObject(j);
                            if (part.getString("type").equals("text")) {
                                sb.append(part.getString("text"));
                            }
                        }
                        if (sb.isEmpty()) {
                            return "Tool call returned no text :(\nFor security concerns some part of the response may be omitted.\n-BukkitHTTP MCP Client";
                        }
                        return sb.toString();
                    } catch (IOException e) {
                        return "Tool call failed :(\n" + e;
                    }
                });
                toolList.add(t);
            }
            if (toolList.isEmpty()) {
                throw new IOException("No valid tools available from MCP");
            }
            return new MCPToolList(rpc, toolList);
        } catch (Exception ex) {
            try {
                rpc.close();
            } catch (Exception ignored) {
            }
            throw new IOException("Failed to communicate with MCP", ex);
        }
    }

    private static Object asType(String type, String input) {
        try {
            return switch (type) {
                case "number" -> Double.parseDouble(input);
                case "integer" -> Integer.parseInt(input);
                case "boolean" -> Boolean.parseBoolean(input);
                default -> input;
            };
        } catch (Exception ex) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            MCPToolList tool = fromMcp("https://mcp.api-inference.modelscope.net/xxx/sse", false);
            OpenRouter.or_key = "sk-or-v1-xxx";
            ChatContext ctx = new ChatContext();
            ctx.tools = tool.tools;
            ctx.noThinking = false;
            ctx.messages.add(new Message("user", "周纯杰是谁？"));
            OpenRouter.complete(ctx, () -> {
                System.out.println(ctx);
                System.out.println("-------------------------------------------------------------------------------------------------------------------");
            });
            System.out.println(ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
