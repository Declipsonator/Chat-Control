package me.declipsonator.chatcontrol.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.declipsonator.chatcontrol.ChatControl;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
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
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JsonObject object = new JsonParser().parse(response.toString()).getAsJsonObject();
            return object.get("name").getAsString();

        } catch (Exception e) {
            ChatControl.LOG.info("Failed to get player name for UUID: " + uuid + ". Maybe you are offline?");
        }

        return uuid;
    }

    public static List<String> getPlayerNames(List<UUID> uuids) {
        List<String> names = new ArrayList<>();
        for(UUID uuid : uuids) {
            names.add(getPlayerName(uuid.toString()));
        }
        return names;
    }
}
