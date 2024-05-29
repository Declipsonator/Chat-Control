package me.declipsonator.chatcontrol.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.declipsonator.chatcontrol.ChatControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerUtils {
    public static String getPlayerName(String uuid) {
        try {
            String urlString = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
            URL url = new URL(urlString);
            JsonObject object = getJsonObject(url);
            return object.get("name").getAsString();

        } catch (Exception e) {
            ChatControl.LOG.info("Failed to get player name for UUID: " + uuid + ". Maybe you are offline?");
        }

        return uuid;
    }

    private static JsonObject getJsonObject(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return JsonParser.parseString(response.toString()).getAsJsonObject();
    }

    public static List<String> getPlayerNames(List<UUID> uuids) {
        List<String> names = new ArrayList<>();
        for(UUID uuid : uuids) {
            names.add(getPlayerName(uuid.toString()));
        }
        return names;
    }
}
