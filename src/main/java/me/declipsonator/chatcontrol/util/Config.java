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
import java.time.Instant;
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
    private static final ArrayList<MutedPlayer> mutedPlayers = new ArrayList<>();
    private static final ArrayList<TempMutedPlayer> tempMutedPlayers = new ArrayList<>();
    private static final ArrayList<UUID> ignoredPlayers = new ArrayList<>();
    public static boolean logFiltered = true;
    public static boolean ignorePrivateMessages = false;
    public static boolean caseSensitive = false;
    public static boolean muteCommand = true;
    public static boolean tellPlayer = true;
    public static boolean censorAndSend = false;

    public static boolean muteAfterOffense = false;

    public static MuteType muteAfterOffenseType = MuteType.TEMPORARY;

    public static int muteAfterOffenseMinutes = 5;

    public static int muteAfterOffenseNumber = 3;

    public static int offenseExpireMinutes = 30;

    public static ArrayList<Offense> offenses = new ArrayList<>();

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

    public static ArrayList<MutedPlayer> getMutedPlayers() {
        return (ArrayList<MutedPlayer>) mutedPlayers.clone();
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

    public static void addMutedPlayer(UUID player, String reason) {
        mutedPlayers.add(new MutedPlayer(player, reason));
    }

    public static void addTempMutedPlayer(UUID player, long time, String reason) {
        tempMutedPlayers.add(new TempMutedPlayer(player, time, reason));
    }

    public static void addReplacementChar(char replacementChar, char replacement) {
        replacementChars.add(new ReplacementChar(replacementChar, replacement));
    }

    public static void addIgnoredPlayer(UUID player) {
        ignoredPlayers.add(player);
    }

    public static void addOffense(UUID player) {
        offenses.add(new Offense(player, Instant.now().getEpochSecond()));
    }

    public static void removeOffenses(UUID player) {
        offenses.removeIf(offense -> offense.uuid().equals(player));
    }

    public static void removeOldOffenses() {
        offenses.removeIf(offense -> Instant.now().getEpochSecond() - offense.time() > offenseExpireMinutes * 60L);
    }

    public static int offenseCount(UUID player) {
        removeOldOffenses();
        int count = 0;
        for(Offense offense : offenses) {
            if(offense.uuid().equals(player)) {
                count++;
            }
        }
        return count;
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
        mutedPlayers.removeIf(mutedPlayer -> mutedPlayer.uuid().equals(player));
        for (TempMutedPlayer tempMutedPlayer : tempMutedPlayers) {
            if (tempMutedPlayer.uuid().equals(player)) {
                removeTempMutedPlayer(tempMutedPlayer);
                break;
            }
        }
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
        return mutedPlayers.stream().anyMatch(mutedPlayer -> mutedPlayer.uuid().equals(player)) || isTempMuted(player);
    }

    public static String getMuteReason(UUID player) {
        for(MutedPlayer mutedPlayer : mutedPlayers) {
            if(mutedPlayer.uuid().equals(player)) {
                return mutedPlayer.reason();
            }
        }

        for(TempMutedPlayer tempMutedPlayer : tempMutedPlayers) {
            if(tempMutedPlayer.uuid().equals(player)) {
                return tempMutedPlayer.reason();
            }
        }

        return "";
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

    public static boolean checkWords(String string) {
        for(String message : replacedCharPossibilities(string))
        {
            message = caseSensitive ? message : message.toLowerCase();
            for (String word : words) {
                String adjustedWord = caseSensitive ? word : word.toLowerCase();
                if (message.contains(adjustedWord)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkStandAloneWords(String string) {
        for(String message : replacedCharPossibilities(string))
        {
            message = caseSensitive ? message : message.toLowerCase();
            for (String standAloneWord : standAloneWords) {
                String adjustedStandAloneWord = caseSensitive ? standAloneWord : standAloneWord.toLowerCase();
                String regex = "\\b" + Pattern.quote(adjustedStandAloneWord) + "\\b";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(message);

                if (matcher.find()) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean checkPhrases(String string) {
        for(String message : replacedCharPossibilities(string))
        {
            message = caseSensitive ? message : message.toLowerCase();
            for (String phrase : phrases) {
                String adjustedPhrase = caseSensitive ? phrase : phrase.toLowerCase();
                if (message.contains(adjustedPhrase)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkRegexes(String string) {
        for(String message : replacedCharPossibilities(string))
        {
            for(String regex : regexes)
            {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(message);

                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int countInString(String message, char c) {
        int count = 0;
        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static String censorWords(String string) {
        String mostFilteredMessage = "";
        for(String message : replacedCharPossibilities(string))
        {
            StringBuilder modifiedMessage = new StringBuilder(message);
            message = caseSensitive ? message : message.toLowerCase();

            for (String word : words) {
                String adjustedWord = caseSensitive ? word : word.toLowerCase();
                if (message.contains(adjustedWord)) {
                    String replacement = "#".repeat(word.length());
                    modifiedMessage = new StringBuilder(modifiedMessage.toString().replace(word, replacement));
                }
            }
            if(countInString(modifiedMessage.toString(), '#') >= countInString(mostFilteredMessage, '#')) {
                mostFilteredMessage = modifiedMessage.toString();
            }
        }
        for (int i = 0; i < mostFilteredMessage.length(); i++) {
            if (mostFilteredMessage.charAt(i) == '#') {
                string = string.substring(0, i) + "#" + string.substring(i + 1);
            }
        }
        return string;
    }

    public static String censorStandAloneWords(String string) {
        String mostFilteredMessage = "";
        for(String message : replacedCharPossibilities(string))
        {
            StringBuilder modifiedMessage = new StringBuilder(message);
            message = caseSensitive ? message : message.toLowerCase();

            for (String standAloneWord : standAloneWords) {
                String adjustedStandAloneWord = caseSensitive ? standAloneWord : standAloneWord.toLowerCase();
                String regex = "\\b" + Pattern.quote(adjustedStandAloneWord) + "\\b";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(message);

                while (matcher.find()) {
                    String replacement = "#".repeat(standAloneWord.length());
                    modifiedMessage.replace(matcher.start(), matcher.end(), replacement);
                    message = modifiedMessage.toString().toLowerCase();
                    matcher = pattern.matcher(message);
                }
            }
            if(countInString(modifiedMessage.toString(), '#') >= countInString(mostFilteredMessage, '#')) {
                mostFilteredMessage = modifiedMessage.toString();
            }
        }
        for (int i = 0; i < mostFilteredMessage.length(); i++) {
            if (mostFilteredMessage.charAt(i) == '#') {
                string = string.substring(0, i) + "#" + string.substring(i + 1);
            }
        }
        return string;
    }

    public static String censorPhrases(String string) {
        String mostFilteredMessage = "";
        for(String message : replacedCharPossibilities(string))
        {
            StringBuilder modifiedMessage = new StringBuilder(message);
            message = caseSensitive ? message : message.toLowerCase();

            for (String phrase : phrases) {
                String adjustedPhrase = caseSensitive ? phrase : phrase.toLowerCase();
                int index = message.indexOf(adjustedPhrase);

                while (index != -1) {
                    String replacement = "#".repeat(phrase.length());
                    modifiedMessage.replace(index, index + adjustedPhrase.length(), replacement);
                    message = modifiedMessage.toString().toLowerCase();
                    index = message.indexOf(adjustedPhrase, index + replacement.length());
                }
            }
            if(countInString(modifiedMessage.toString(), '#') >= countInString(mostFilteredMessage, '#')) {
                mostFilteredMessage = modifiedMessage.toString();
            }
        }
        for (int i = 0; i < mostFilteredMessage.length(); i++) {
            if (mostFilteredMessage.charAt(i) == '#') {
                string = string.substring(0, i) + "#" + string.substring(i + 1);
            }
        }
        return string;
    }

    public static String censorRegexes(String string) {
        String mostFilteredMessage = "";
        for(String message : replacedCharPossibilities(string))
        {
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
            if(countInString(modifiedMessage.toString(), '#') >= countInString(mostFilteredMessage, '#')) {
                mostFilteredMessage = modifiedMessage.toString();
            }
        }
        for (int i = 0; i < mostFilteredMessage.length(); i++) {
            if (mostFilteredMessage.charAt(i) == '#') {
                string = string.substring(0, i) + "#" + string.substring(i + 1);
            }
        }
        return string;
    }


    public static ArrayList<String> replacedCharPossibilities(String input) {
        ArrayList<String> results = new ArrayList<>();
        generateReplacementsHelper(input.toCharArray(), 0, results);
        return results;
    }

    // Helper function for recursion
    private static void generateReplacementsHelper(char[] input, int index, ArrayList<String> results) {
        if (index == input.length) {
            results.add(new String(input));
            return;
        }

        // Generate without replacement
        generateReplacementsHelper(input, index + 1, results);

        // Generate with replacement if applicable
        for (ReplacementChar rc : replacementChars) {
            if (input[index] == rc.toReplace) {
                char originalChar = input[index];
                input[index] = rc.replaceWith;
                generateReplacementsHelper(input, index + 1, results);
                input[index] = originalChar; // revert back for next iterations
            }
        }
    }


    public static void loadConfig() {
        File file = ChatControl.configFilePath.toFile();
        if(!file.exists()) {
            saveConfig();
            return;
        }
        try(FileReader fileReader = new FileReader(file)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(MutedPlayer.class, new MutedPlayerTypeAdapter())
                    .create();
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
            muteAfterOffense = config.muteAfterOffense;
            muteAfterOffenseType = config.muteAfterOffenseType;
            muteAfterOffenseMinutes = config.muteAfterOffenseMinutes;
            muteAfterOffenseNumber = config.muteAfterOffenseNumber;
            offenseExpireMinutes = config.offenseExpireMinutes;
            offenses.addAll(config.offenses);


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
            jsonObject.add("mutedPlayers", mutedPlayersToJsonArray(mutedPlayers));
            jsonObject.add("tempMutedPlayers", tempMutedPlayersToJsonArray(tempMutedPlayers));
            jsonObject.add("ignoredPlayers", arrayListToJsonArray(ignoredPlayers));
            jsonObject.addProperty("logFiltered", logFiltered);
            jsonObject.addProperty("ignorePrivateMessages", ignorePrivateMessages);
            jsonObject.addProperty("caseSensitive", caseSensitive);
            jsonObject.addProperty("muteCommand", muteCommand);
            jsonObject.addProperty("tellPlayer", tellPlayer);
            jsonObject.addProperty("censorAndSend", censorAndSend);
            jsonObject.addProperty("muteAfterOffense", muteAfterOffense);
            jsonObject.addProperty("muteAfterOffenseType", muteAfterOffenseType.toString());
            jsonObject.addProperty("muteAfterOffenseMinutes", muteAfterOffenseMinutes);
            jsonObject.addProperty("muteAfterOffenseNumber", muteAfterOffenseNumber);
            jsonObject.addProperty("offenseExpireMinutes", offenseExpireMinutes);
            jsonObject.add("offenses", offensesToJsonArray(offenses));

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
            jsonObject.addProperty("reason", tempMutedPlayer.reason());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public static JsonArray mutedPlayersToJsonArray(ArrayList<MutedPlayer> list) {
        JsonArray jsonArray = new JsonArray();
        for(MutedPlayer tempMutedPlayer : list) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", tempMutedPlayer.uuid().toString());
            jsonObject.addProperty("reason", tempMutedPlayer.reason());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public static JsonArray offensesToJsonArray(ArrayList<Offense> list) {
        JsonArray jsonArray = new JsonArray();
        for(Offense offense : list) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", offense.uuid().toString());
            jsonObject.addProperty("time", offense.time());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public enum MuteType {
        PERMANENT,
        TEMPORARY
    }
}