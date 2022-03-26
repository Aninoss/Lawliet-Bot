package core.botlists;

import core.ExceptionLogger;
import core.ShardManager;
import core.internet.HttpHeader;
import core.internet.HttpRequest;
import org.json.JSONObject;

public class Discordbotlist {

    public static void updateServerCount(long serverCount) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("guilds", serverCount);

        HttpHeader httpHeader = new HttpHeader("Authorization", System.getenv("DISCORDBOTLIST_TOKEN"));
        HttpRequest.post(String.format("https://discordbotlist.com/api/v1/bots/%s/stats", ShardManager.getSelfId()), "application/json", jsonObject.toString(), httpHeader)
                .exceptionally(ExceptionLogger.get());
    }

}
