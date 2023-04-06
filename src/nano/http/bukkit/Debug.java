package nano.http.bukkit;

import nano.http.d2.utils.Request;

public class Debug {
    public static void main(String[] args) {
        String msg = "{\n" +
                "  \"msg_type\": \"post\",\n" +
                "  \"content\": {\n" +
                "    \"post\": {\n" +
                "      \"zh-CN\": {\n" +
                "        \"title\": \"[UniMsg]>来自Buddy_Feedback的消息推送\",\n" +
                "        \"content\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"tag\": \"text\",\n" +
                "              \"text\": \"新的用户反馈：求助，这个Bug怎么解决！\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"tag\": \"a\",\n" +
                "              \"text\": \"点击查看\",\n" +
                "              \"href\": \"http://www.baidu.com/\"\n" +
                "            }\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        System.out.println(Request.jsonPost("https://open.feishu.cn/open-apis/bot/v2/hook/78719bb9-7ecb-4f2b-9bb0-6eedca0889ce", msg, null));
    }
}
