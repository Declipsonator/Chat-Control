package me.declipsonator.chatcontrol.util;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.declipsonator.chatcontrol.ChatControl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    private static final ArrayList<String> regexes = new ArrayList<>();
    private static final ArrayList<String> phrases = new ArrayList<>();
    private static final ArrayList<String> words = new ArrayList<>();

    private static final ArrayList<String> standAloneWords = new ArrayList<>();
    private static final ArrayList<ReplacementChar> replacementChars = new ArrayList<>();
    private static final ArrayList<UUID> mutedPlayers = new ArrayList<>();
    private static final ArrayList<TempMutedPlayer> tempMutedPlayers = new ArrayList<>();
    private static final ArrayList<UUID> ignoredPlayers = new ArrayList<>();
    public static boolean logFiltered = true;
    public static boolean ignorePrivateMessages = false;
    public static boolean caseSensitive = false;
    public static boolean muteCommand = true;
    public static boolean tellPlayer = true;
    public static boolean censorAndSend = false;

    public static ArrayList<String> getRegexes() {
        return (ArrayList<String>) regexes.clone();
    }

    public static ArrayList<String> getPhrases() {
        return (ArrayList<String>) phrases.clone();
    }

    public static ArrayList<String> getWords() {
        return (ArrayList<String>) words.clone();
    }

    public static ArrayList<String> getStandAloneWords() {
        return (ArrayList<String>) standAloneWords.clone();
    }

    public static ArrayList<UUID> getMutedPlayers() {
        return (ArrayList<UUID>) mutedPlayers.clone();
    }

    public static ArrayList<TempMutedPlayer> getTempMutedPlayers() {
        return (ArrayList<TempMutedPlayer>) tempMutedPlayers.clone();
    }

    public static ArrayList<ReplacementChar> getReplacementChars() {
        return (ArrayList<ReplacementChar>) replacementChars.clone();
    }

    public static ArrayList<UUID> getIgnoredPlayers() {
        return (ArrayList<UUID>) ignoredPlayers.clone();
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

    public static void addStandAloneWord(String standAloneWord) {
        standAloneWords.add(standAloneWord);
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

    public static void removeStandAloneWord(String standAloneWord) {
        standAloneWords.remove(standAloneWord);
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

    public static boolean isPhrase(String phrase) {
        return phrases.contains(phrase);
    }

    public static boolean isWord(String word) {
        return words.contains(word);
    }

    public static boolean isStandAloneWord(String standAloneWord) {
        return standAloneWords.contains(standAloneWord);
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

    public static long timeLeftTempMuted(UUID player) {
        for(TempMutedPlayer tempMutedPlayer : tempMutedPlayers) {
            if(tempMutedPlayer.uuid().equals(player)) {
                if(System.currentTimeMillis() >= tempMutedPlayer.until()) {
                    removeTempMutedPlayer(tempMutedPlayer);
                    return 0;
                }
                return tempMutedPlayer.until() - System.currentTimeMillis();
            }
        }
        return 0;
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
            if (message.contains(adjustedWord)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkStandAloneWords(String message) {
        message = caseSensitive ? replaceChars(message) : replaceChars(message).toLowerCase();
        for (String standAloneWord : standAloneWords) {
            String adjustedStandAloneWord = caseSensitive ? standAloneWord : standAloneWord.toLowerCase();
            String regex = "\\b" + Pattern.quote(adjustedStandAloneWord) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
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
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                return true;
            }
        }

        return false;
    }

    public static String censorWords(String message) {
        StringBuilder modifiedMessage = new StringBuilder(message);
        message = caseSensitive ? replaceChars(message) : replaceChars(message).toLowerCase();

        for (String word : words) {
            String adjustedWord = caseSensitive ? word : word.toLowerCase();
            if (message.contains(adjustedWord)) {
                String replacement = "#".repeat(word.length());
                modifiedMessage = new StringBuilder(modifiedMessage.toString().replace(word, replacement));
            }
        }

        return modifiedMessage.toString();
    }

    public static String censorStandAloneWords(String message) {
        StringBuilder modifiedMessage = new StringBuilder(message);
        message = caseSensitive ? replaceChars(message) : replaceChars(message).toLowerCase();

        for (String standAloneWord : standAloneWords) {
            String adjustedStandAloneWord = caseSensitive ? standAloneWord : standAloneWord.toLowerCase();
            String regex = "\\b" + Pattern.quote(adjustedStandAloneWord) + "\\b";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String replacement = "#".repeat(standAloneWord.length());
                modifiedMessage.replace(matcher.start(), matcher.end(), replacement);
                matcher = pattern.matcher(modifiedMessage);
            }
        }

        return modifiedMessage.toString();
    }

    public static String censorPhrases(String message) {
        StringBuilder modifiedMessage = new StringBuilder(message);
        message = caseSensitive ? replaceChars(message) : replaceChars(message).toLowerCase();

        for (String phrase : phrases) {
            String adjustedPhrase = caseSensitive ? phrase : phrase.toLowerCase();
            int index = message.toLowerCase().indexOf(adjustedPhrase);

            while (index != -1) {
                String replacement = "#".repeat(phrase.length());
                modifiedMessage.replace(index, index + adjustedPhrase.length(), replacement);
                message = modifiedMessage.toString().toLowerCase();
                index = message.indexOf(adjustedPhrase, index + replacement.length());
            }
        }

        return modifiedMessage.toString();
    }

    public static String censorRegexes(String message) {
        StringBuilder modifiedMessage = new StringBuilder(message);

        for (String regex : regexes) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String replacement = "#".repeat(matcher.group().length());
                modifiedMessage.replace(matcher.start(), matcher.end(), replacement);
                matcher = pattern.matcher(modifiedMessage);
            }
        }

        return modifiedMessage.toString();
    }



    public static String replaceChars(String message) {
        if(caseSensitive) {
            for(ReplacementChar replacementChar : replacementChars) {
                message = message.replace(replacementChar.toReplace, replacementChar.replaceWith);
            }
            return message;
        }
        for(ReplacementChar replacementChar : replacementChars) {
            message = message.toLowerCase().replace(Character.toLowerCase(replacementChar.toReplace), replacementChar.replaceWith);
        }
        return message;
    }


    public static void loadConfig() {
        File file = ChatControl.configFilePath.toFile();
        if(!file.exists()) {
            saveConfig();
            return;
        }
        try(FileReader fileReader = new FileReader(file)) {
            Gson gson = new GsonBuilder().create();
            Type configType = new TypeToken<ConfigTemplate>() {}.getType();
            ConfigTemplate config = gson.fromJson(fileReader, configType);

            regexes.addAll(config.regexes);
            phrases.addAll(config.phrases);
            words.addAll(config.words);
            standAloneWords.addAll(config.standAloneWords);
            replacementChars.addAll(config.replacementChars);
            mutedPlayers.addAll(config.mutedPlayers);
            tempMutedPlayers.addAll(config.tempMutedPlayers);
            ignoredPlayers.addAll(config.ignoredPlayers);
            logFiltered = config.logFiltered;
            ignorePrivateMessages = config.ignorePrivateMessages;
            caseSensitive = config.caseSensitive;
            muteCommand = config.muteCommand;
            tellPlayer = config.tellPlayer;
            censorAndSend = config.censorAndSend;

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
            jsonObject.add("standAloneWords", arrayListToJsonArray(standAloneWords));
            jsonObject.add("replacementChars", replacementsToJsonArray(replacementChars));
            jsonObject.add("mutedPlayers", arrayListToJsonArray(mutedPlayers));
            jsonObject.add("tempMutedPlayers", tempMutedPlayersToJsonArray(tempMutedPlayers));
            jsonObject.add("ignoredPlayers", arrayListToJsonArray(ignoredPlayers));
            jsonObject.addProperty("logFiltered", logFiltered);
            jsonObject.addProperty("ignorePrivateMessages", ignorePrivateMessages);
            jsonObject.addProperty("caseSensitive", caseSensitive);
            jsonObject.addProperty("muteCommand", muteCommand);
            jsonObject.addProperty("tellPlayer", tellPlayer);
            jsonObject.addProperty("censorAndSend", censorAndSend);

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
}