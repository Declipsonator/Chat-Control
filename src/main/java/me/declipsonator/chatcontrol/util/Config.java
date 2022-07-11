package me.declipsonator.chatcontrol.util;


import com.google.gson.*;
import me.declipsonator.chatcontrol.ChatControl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

public class Config {
    private static final ArrayList<String> regexes = new ArrayList<>();
    private static final ArrayList<String> phrases = new ArrayList<>();
    private static final ArrayList<String> words = new ArrayList<>();
    private static final ArrayList<ReplacementChar> replacementChars = new ArrayList<>();
    private static final ArrayList<UUID> mutedPlayers = new ArrayList<>();
    private static final ArrayList<TempMutedPlayer> tempMutedPlayers = new ArrayList<>();
    private static final ArrayList<UUID> ignoredPlayers = new ArrayList<>();
    public static boolean logFiltered = true;
    public static boolean ignoreCommands = true;
    public static boolean caseSensitive = false;
    public static boolean muteCommand = true;

    public static ArrayList<String> getRegexes() {
        return (ArrayList<String>) regexes.clone();
    }

    public static ArrayList<String> getPhrases() {
        return (ArrayList<String>) phrases.clone();
    }

    public static ArrayList<String> getWords() {
        return (ArrayList<String>) words.clone();
    }

    public static ArrayList<UUID> getMutedPlayers() {
        return (ArrayList<UUID>) mutedPlayers.clone();
    }

    public static ArrayList<ReplacementChar> getReplacementChars() {
        return (ArrayList<ReplacementChar>) replacementChars.clone();
    }

    public static ArrayList<UUID> getIgnoredPlayers() {
        return (ArrayList<UUID>) ignoredPlayers.clone();
    }

    public static ArrayList<TempMutedPlayer> getTempMutedPlayers() {
        return (ArrayList<TempMutedPlayer>) tempMutedPlayers.clone();
    }

    public static void addRegex(String regex) {
        regexes.add(regex);
    }

    public static void addPhrase(String phrase) {
        phrases.add(phrase);
    }

    public static void addWord(String word) {
        words.add(word);
    }

    public static void addMutedPlayer(UUID player) {
        mutedPlayers.add(player);
    }

    public static void addTempMutedPlayer(UUID player, long time) {
        tempMutedPlayers.add(new TempMutedPlayer(player, time));
    }

    public static void addReplacementChar(char replacementChar, char replacement) {
        replacementChars.add(new ReplacementChar(replacementChar, replacement));
    }

    public static void addIgnoredPlayer(UUID player) {
        ignoredPlayers.add(player);
    }

    public static void removeRegex(String regex) {
        regexes.remove(regex);
    }

    public static void removePhrase(String phrase) {
        phrases.remove(phrase);
    }

    public static void removeWord(String word) {
        words.remove(word);
    }

    public static void removeMutedPlayer(UUID player) {
        mutedPlayers.remove(player);
    }

    public static void removeTempMutedPlayer(TempMutedPlayer player) {
        tempMutedPlayers.remove(player);
    }

    public static void removeReplacementChar(char replacementChar, char replaceWith) {
        replacementChars.removeIf(replacementChar1 -> replacementChar1.toReplace == replacementChar && replacementChar1.replaceWith == replaceWith);
    }

    public static void removeIgnoredPlayer(UUID player) {
        ignoredPlayers.remove(player);
    }

    public static boolean isRegex(String word) {
        return regexes.contains(word);
    }

    public static boolean isPhrase(String word) {
        return phrases.contains(word);
    }

    public static boolean isWord(String word) {
        return words.contains(word);
    }

    public static boolean isMuted(UUID player) {
        return mutedPlayers.contains(player) || isTempMuted(player);
    }

    public static boolean isTempMuted(UUID player) {
        for(TempMutedPlayer tempMutedPlayer : tempMutedPlayers) {
            if(tempMutedPlayer.uuid().equals(player)) {
                if(System.currentTimeMillis() >= tempMutedPlayer.until()) {
                    removeTempMutedPlayer(tempMutedPlayer);
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    public static boolean isReplacementChar(ReplacementChar look) {

        for(ReplacementChar replacementChar : replacementChars) {
            if(replacementChar.toReplace == look.toReplace
            && replacementChar.replaceWith == look.replaceWith) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIgnored(UUID player) {
        return ignoredPlayers.contains(player);
    }

    public static boolean checkWords(String message) {
        message = caseSensitive ? replaceChars(message) : replaceChars(message).toLowerCase();
        for (String word : words) {
            String adjustedWord = caseSensitive ? word : word.toLowerCase();
            String regex = "\\W*((?i)" + Pattern.quote(adjustedWord) + "(?-i))\\W*";

            if (message.matches(regex)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkPhrases(String message) {
        message = caseSensitive ? replaceChars(message) : replaceChars(message).toLowerCase();
        for (String phrase : phrases) {
            String adjustedPhrase = caseSensitive ? phrase : phrase.toLowerCase();
            if (message.toLowerCase().contains(adjustedPhrase)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkRegexes(String message) {
        for (String regex : regexes) {
            if(caseSensitive) {
                if (message.matches(regex)) {
                    return true;
                }
            } else if (message.matches("(?i)" + regex)) {
                return true;
            }
        }

        return false;
    }

    public static String replaceChars(String message) {
        for(ReplacementChar replacementChar : replacementChars) {
            message = message.replace(replacementChar.toReplace, replacementChar.replaceWith);
        }
        return message;
    }


    public static void loadConfig() {
        File file = ChatControl.configFilePath.toFile();
        if(!file.exists()) {
            saveConfig();
            return;
        }
        try {
            String json = new String(Files.readAllBytes(ChatControl.configFilePath)).replace("\n", "");
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            regexes.addAll(jsonArrayToArrayList(jsonObject.get("regexes").getAsJsonArray()));
            phrases.addAll(jsonArrayToArrayList(jsonObject.get("phrases").getAsJsonArray()));
            words.addAll(jsonArrayToArrayList(jsonObject.get("words").getAsJsonArray()));
            replacementChars.addAll(jsonArrayToReplacements(jsonObject.get("replacementChars").getAsJsonArray()));
            mutedPlayers.addAll(jsonArrayToUUIDs(jsonObject.get("mutedPlayers").getAsJsonArray()));
            tempMutedPlayers.addAll(jsonArrayToTempMutedPlayers(jsonObject.get("tempMutedPlayers").getAsJsonArray()));
            ignoredPlayers.addAll(jsonArrayToUUIDs(jsonObject.get("ignoredPlayers").getAsJsonArray()));
            logFiltered = jsonObject.get("logFiltered").getAsBoolean();
            ignoreCommands = jsonObject.get("ignoreCommands").getAsBoolean();
            caseSensitive = jsonObject.get("caseSensitive").getAsBoolean();
            muteCommand = jsonObject.get("muteCommand").getAsBoolean();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("regexes", arrayListToJsonArray(regexes));
            jsonObject.add("phrases", arrayListToJsonArray(phrases));
            jsonObject.add("words", arrayListToJsonArray(words));
            jsonObject.add("replacementChars", replacementsToJsonArray(replacementChars));
            jsonObject.add("mutedPlayers", arrayListToJsonArray(mutedPlayers));
            jsonObject.add("tempMutedPlayers", tempMutedPlayersToJsonArray(tempMutedPlayers));
            jsonObject.add("ignoredPlayers", arrayListToJsonArray(ignoredPlayers));
            jsonObject.addProperty("logFiltered", logFiltered);
            jsonObject.addProperty("ignoreCommands", ignoreCommands);
            jsonObject.addProperty("caseSensitive", caseSensitive);
            jsonObject.addProperty("muteCommand", muteCommand);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(jsonObject);
            Files.write(ChatControl.configFilePath, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonArray arrayListToJsonArray(ArrayList<?> list) {
        JsonArray jsonArray = new JsonArray();
        for(Object object : list) {
            jsonArray.add(object.toString());
        }
        return jsonArray;
    }

    public static JsonArray replacementsToJsonArray(ArrayList<ReplacementChar> list) {
        JsonArray jsonArray = new JsonArray();
        for(ReplacementChar replacementChar : list) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("toReplace", replacementChar.toReplace);
            jsonObject.addProperty("replaceWith", replacementChar.replaceWith);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public static JsonArray tempMutedPlayersToJsonArray(ArrayList<TempMutedPlayer> list) {
        JsonArray jsonArray = new JsonArray();
        for(TempMutedPlayer tempMutedPlayer : list) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", tempMutedPlayer.uuid().toString());
            jsonObject.addProperty("until", tempMutedPlayer.until());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }


    public static ArrayList<String> jsonArrayToArrayList(JsonArray jsonArray) {
        ArrayList<String> list = new ArrayList<>();
        for(JsonElement element : jsonArray) {
            list.add(element.getAsString());
        }
        return list;
    }

    public static ArrayList<ReplacementChar> jsonArrayToReplacements(JsonArray jsonArray) {
        ArrayList<ReplacementChar> list = new ArrayList<>();
        for(JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            list.add(new ReplacementChar(jsonObject.get("toReplace").getAsString().charAt(0), jsonObject.get("replaceWith").getAsString().charAt(0)));
        }
        return list;
    }

    public static ArrayList<UUID> jsonArrayToUUIDs(JsonArray jsonArray) {
        ArrayList<UUID> list = new ArrayList<>();
        for(JsonElement element : jsonArray) {
            list.add(UUID.fromString(element.getAsString()));
        }
        return list;
    }

    public static ArrayList<TempMutedPlayer> jsonArrayToTempMutedPlayers(JsonArray jsonArray) {
        ArrayList<TempMutedPlayer> list = new ArrayList<>();
        for(JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            list.add(new TempMutedPlayer(UUID.fromString(jsonObject.get("uuid").getAsString()), jsonObject.get("until").getAsLong()));
        }
        return list;
    }



}




