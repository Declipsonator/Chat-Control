package me.declipsonator.chatcontrol.util;

import java.util.ArrayList;
import java.util.UUID;

public class ConfigTemplate {
    public ArrayList<String> regexes = new ArrayList<>();
    public ArrayList<String> phrases = new ArrayList<>();
    public ArrayList<String> words = new ArrayList<>();
    public ArrayList<String> standAloneWords = new ArrayList<>();
    public ArrayList<ReplacementChar> replacementChars = new ArrayList<>();
    public ArrayList<MutedPlayer> mutedPlayers = new ArrayList<>();
    public ArrayList<TempMutedPlayer> tempMutedPlayers = new ArrayList<>();
    public ArrayList<UUID> ignoredPlayers = new ArrayList<>();
    public boolean logFiltered = true;
    public boolean ignorePrivateMessages = false;
    public boolean caseSensitive = false;
    public boolean muteCommand = true;
    public boolean tellPlayer = true;
    public boolean censorAndSend = false;
    public boolean muteAfterOffense = false;
    public Config.MuteType muteAfterOffenseType = Config.MuteType.TEMPORARY;
    public int muteAfterOffenseMinutes = 5;
    public int muteAfterOffenseNumber = 3;
    public int offenseExpireMinutes = 30;
    public ArrayList<Offense> offenses = new ArrayList<>();
}
