package nano.http.d2.ai;

import nano.http.d2.ai.data.ChatContext;
import nano.http.d2.ai.data.Message;
import nano.http.d2.json.JSONArray;
import nano.http.d2.json.NanoJSON;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Consumer;

public class OpenRouter {
    public static String or_key;

    public static void stream(ChatContext ctx, Consumer<StreamingResult> callback) {
        ctx.cancelled = false;
        StreamingResult sr = new StreamingResult();
        if (or_key == null || or_key.isEmpty()) {
            sr.isError = true;
            sr.isFinished = true;
            sr.delta = "OpenRouter API key not set";
            sr.text.append(sr.delta);
            callback.accept(sr);
            return;
        }
        if (!ctx.tools.isEmpty()) {
            sr.isError = true;
            sr.isFinished = true;
            sr.delta = "Streaming with tools is not supported";
            sr.text.append(sr.delta);
            callback.accept(sr);
            return;
        }

        String lastText = null;
        try {
            URL endpoint = new URL("https://openrouter.ai/api/v1/chat/completions");
            HttpURLConnection con = (HttpURLConnection) endpoint.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + or_key);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("X-Title", "BukkitHTTP");
            con.setRequestProperty("HTTP-Referer", "https://github.com/BukkitHTTP/BukkitHTTP");
            con.setDoOutput(true);
            JSONArray messages = new JSONArray();

            for (Message msg : ctx.messages) {
                messages.put(new NanoJSON()
                        .put("role", msg.role)
                        .put("content", msg.content));
            }
            NanoJSON body = new NanoJSON()
                    .put("model", ctx.model)
                    .put("messages", messages)
                    .put("stream", true);
            NanoJSON reasoning = new NanoJSON().put("enabled", !ctx.noThinking);
            body.put("reasoning", reasoning);
            con.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));

            InputStream is;
            try {
                is = con.getInputStream();
            } catch (Exception ex) {
                is = con.getErrorStream();
            }
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Scanner scanner = new Scanner(reader);
            while (!ctx.cancelled && scanner.hasNextLine()) {
                lastText = scanner.nextLine();
                if (lastText.startsWith("data: {")) {
                    lastText = lastText.substring(6);
                    NanoJSON data = new NanoJSON(lastText);
                    data = data.getJSONArray("choices").getJSONObject(0).getJSONObject("delta");


                    sr.delta = data.getString("content");
                    sr.text.append(sr.delta);

                    if (data.hasNonNull("reasoning")) {
                        sr.reasoning.append(data.getString("reasoning"));
                    }
                    callback.accept(sr);
                }
            }
        } catch (Exception ex) {
            sr.isError = true;
            if (sr.text.isEmpty()) {
                if (lastText != null) {
                    sr.delta = "Error: " + lastText;
                } else {
                    sr.delta = "Error: " + ex;
                }
                sr.text.append(sr.delta);
            }
        } finally {
            sr.isFinished = true;
            callback.accept(sr);

            if (sr.isError) {
                ctx.messages.add(new Message("assistant", "Error: " + sr.text));
            } else {
                ctx.messages.add(new Message("assistant", sr.text.toString()));
            }
        }
    }

    public static void complete(ChatContext ctx) {
        complete(ctx, null);
    }

    public static void complete(ChatContext ctx, Runnable toolPartialCallback) {
        ctx.cancelled = false;
        if (or_key == null || or_key.isEmpty()) {
            ctx.messages.add(new Message("assistant", "Error: OpenRouter API key not set"));
        }
        boolean needTrim = false;
        boolean needSkip = true;
        int iterLimit = 20;
        try {
            String lastArg = null;
            while (true) {
                iterLimit--;
                if (iterLimit < 0) {
                    ctx.cancelled = true;
                }
                if (ctx.cancelled) {
                    ctx.messages.add(new Message("assistant", "Error: cancelled"));
                    break;
                }
                URL endpoint = new URL("https://openrouter.ai/api/v1/chat/completions");
                HttpURLConnection con = (HttpURLConnection) endpoint.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Bearer " + or_key);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("X-Title", "BukkitHTTP");
                con.setRequestProperty("HTTP-Referer", "https://github.com/BukkitHTTP/BukkitHTTP");
                con.setDoOutput(true);
                JSONArray messages = new JSONArray();

                for (Message msg : ctx.messages) {
                    NanoJSON m = new NanoJSON();
                    m.put("role", msg.role);

                    if (msg.trans && !ctx.noThinking && msg.role.equals("assistant")) {
                        m.put("content", "");
                        m.put("reasoning", msg.content);
                    } else {
                        m.put("content", msg.content);
                    }

                    if (msg.tool_calling_id != null) {
                        m.put("tool_call_id", msg.tool_calling_id);
                    }
                    if (msg.tool_called != null) {
                        m.put("tool_calls", msg.tool_called);
                    }
                    if (msg.reasoning != null) {
                        m.put("reasoning", msg.reasoning);
                    }


                    messages.put(m);
                }
                NanoJSON body = new NanoJSON()
                        .put("model", ctx.model)
                        .put("messages", messages)
                        .put("stream", false);
                NanoJSON reasoning = new NanoJSON()
                        .put("enabled", !ctx.noThinking)
                        .put("effort", "low");
                body.put("reasoning", reasoning);
                if (!ctx.tools.isEmpty()) {
                    JSONArray tools = new JSONArray();
                    for (Tool t : ctx.tools) {
                        tools.put(t.desc);
                    }
                    body.put("tools", tools);

                    NanoJSON providers = new NanoJSON();
                    JSONArray quantizations = new JSONArray();
                    // X int4 int8 -> too bad to be useful
                    quantizations.put("fp8");
                    quantizations.put("bf16");
                    quantizations.put("fp16");
                    quantizations.put("fp32");
                    providers.put("quantizations", quantizations);
                    body.put("providers", providers);
                }


                con.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));
                InputStream is;
                try {
                    is = con.getInputStream();
                } catch (Exception ex) {
                    is = con.getErrorStream();
                    ctx.messages.add(new Message("assistant", "Error: " + new String(is.readAllBytes(), StandardCharsets.UTF_8)));
                    return;
                }
                NanoJSON response = new NanoJSON(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                response = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message");

                if (!ctx.tools.isEmpty()) {
                    if (response.hasNonNull("tool_calls")) {
                        JSONArray calls = response.getJSONArray("tool_calls");
                        if (!calls.isEmpty()) {
                            Message memoHolder = new Message("assistant", response.getString("content"));
                            if (response.hasNonNull("reasoning")) {
                                memoHolder.content = response.getString("reasoning");
                            }
                            memoHolder.tool_called = calls;
                            memoHolder.trans = true;

                            ctx.messages.add(memoHolder);
                            needTrim = true;
                            // Skip partial callback here because local tool call is fast enough.

                            for (int i = 0; i < calls.length(); i++) {
                                NanoJSON call = calls.getJSONObject(i);

                                String callId = call.getString("id");
                                NanoJSON function = call.getJSONObject("function");
                                String funcName = function.getString("name");
                                NanoJSON args = new NanoJSON(function.getString("arguments"));

                                Tool foundTool = null;
                                for (Tool t : ctx.tools) {
                                    if (t.funcName.equals(funcName)) {
                                        if (foundTool != null) {
                                            throw new Exception("Multiple tools with same name: " + funcName);
                                        }
                                        foundTool = t;
                                    }
                                }
                                if (foundTool == null) {
                                    throw new Exception("No tool with name: " + funcName);
                                }

                                String resp = "Tool-Janitor: You can NOT call the same tool with the same argument twice in a row. Refusing to call tool. YOU WILL BE BANNED FROM TOOL USAGE IF YOU KEEP DOING THIS.";
                                String strArg = args.getString(foundTool.queryName);
                                if (!strArg.equals(lastArg)) {
                                    resp = foundTool.func.apply(strArg);
                                }
                                lastArg = strArg;

                                if (toolPartialCallback != null) {
                                    toolPartialCallback.run();
                                }
                                Message toolMsg = new Message("tool", resp);
                                toolMsg.trans = true;
                                toolMsg.tool_calling_id = callId;
                                ctx.messages.add(toolMsg);
                            }
                            continue;
                        }
                    }
                }

                String c = response.getString("content");
                String r = response.optString("reasoning", "");
                if (!ctx.tools.isEmpty() && !ctx.noThinking && c.isEmpty() && !r.isEmpty()) {
                    if (needTrim) {
                        if (needSkip) {
                            needSkip = false;
                            Message msg = new Message("assistant", r);
                            msg.trans = true;
                            ctx.messages.add(msg);
                            if (toolPartialCallback != null) {
                                toolPartialCallback.run();
                            }
                            continue;
                        }
                        c = r;
                    } else {
                        ctx.messages.add(new Message("assistant", "Error: Model returned reasoning without content, try enabling NoThinking! Reasoning: " + r));
                        break;
                    }
                }

                ctx.messages.add(new Message("assistant", c));
                break;
            }
        } catch (Exception ex) {
            ctx.messages.add(new Message("assistant", "Error: " + ex));
        } finally {
            if (needTrim) {
                ctx.trimTrans();
            }
        }
    }
}
