package me.declipsonator.chatcontrol.util;

import java.util.ArrayList;
import java.util.UUID;

public class ConfigTemplate {
    public final ArrayList<String> regexes = new ArrayList<>();
    public final ArrayList<String> phrases = new ArrayList<>();
    public final ArrayList<String> words = new ArrayList<>();
    public final ArrayList<String> standAloneWords = new ArrayList<>();
    public final ArrayList<ReplacementChar> replacementChars = new ArrayList<>();
    public final ArrayList<UUID> mutedPlayers = new ArrayList<>();
    public final ArrayList<TempMutedPlayer> tempMutedPlayers = new ArrayList<>();
    public final ArrayList<UUID> ignoredPlayers = new ArrayList<>();
    public boolean logFiltered = true;
    public boolean ignorePrivateMessages = false;
    public boolean caseSensitive = false;
    public boolean muteCommand = true;
    public boolean tellPlayer = true;
    public boolean censorAndSend = false;
}
