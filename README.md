# Chat Control: Ultimate Server Protection

🛡️ Protect your server and the people on it with the ultimate system of filtering and muting! With Chat Control, you can ensure a safe and enjoyable environment for all players. Don't let disruptive messages ruin the fun - take control today!

🔒 It is highly recommended to set `enforce-secure-profile` to false in your server.properties to maximize user protection and prevent any conflicts with the mod.

## Download
Chat Control can be downloaded on [Modrinth](https://modrinth.com/mod/chat) and [Curseforge](https://www.curseforge.com/minecraft/mc-mods/chat-control).

## 🌟 Features That Help You:

- Word Filtering: Filter out unwanted words and maintain a positive atmosphere on your server.
- Standalone Word Filtering: Target specific words that are on their own (for example `forts` would not be flagged when filtering `fort`
- Phrase Filtering: Keep an eye out for phrases that may be inappropriate or disruptive.
- Filtering With Regex: Utilize the power of regex to enforce complex filtering patterns.
- Intelligent Letter Replacement: Replace certain letters before filtering (e.g., f1rst -> first), making the filtering even more effective.
- Flexible Message Handling: Choose to censor messages or drop them altogether, giving you full control over what's displayed.
- Permanent Player Muting: Silence troublemakers for good, ensuring a peaceful server environment.
- Temporary Player Muting: Temporarily mute players for a specific duration, granting them a chance to reflect on their behavior.
- Filtered Message Logging: Receive reports of filtered messages in a log, allowing you to monitor the effectiveness of the filtering system.
- Ignore Private Messages Option: Opt to exclude private messages from filtering, promoting private conversations while still maintaining a safe public chat.
- Case Sensitivity Option: Fine-tune your filters by enabling or disabling case sensitivity.
- Automatic Player Muting: Don't worry about being online 24/7 - With muteAfterOffense you can choose how many offenses constitute a mute, how long the mute is, when the offenses expire, and you can sleep in peace.

## 💬 Command Tutorial: Take Charge!

### Add a Filter:
/filter add {word|standAloneWord|phrase|regex} {text}

### Remove a Filter:
/filter remove {word|standAloneWord|phrase|regex} {text}

### View Filters:
/filter list {all|word|standAloneWord|phrase|regex}
 
### Manage Replacement Letters:
/filter config replacementLetters add {letter to replace} {letter to replace with}\
/filter config replacementLetters remove {letter to replace} {letter to replace with}

### Manage Ignored Players:
/filter config ignoredPlayers add {player}
/filter config ignoredPlayers remove {player}

### Mute a Player:
/mute add permanent {player} {reason}

### Temporarily Mute a Player:
/mute add temporary {player} {minutes} {reason}

### Remove a Mute:
/mute remove {player}

### Automatically Mute a Player:
/filter config muteAfterOffense set {true|false}\
/filter config muteAfterOffense type {PERMANENT|TEMPORARY}\
/filter config muteAfterOffense number {number_of_offenses}\
/filter config muteAfterOffense length {minutes_of_mute}\
/filter config muteAfterOffense expireMinutes {offense_expire_minutes}\
/filter config muteAfterOffense currentOffenses\

### Change Settings:
/filter config {logFiltered|ignorePrivateMessages|caseSensitive|tellPlayer|censorAndSend} {true|false}

## ℹ️ Info: Frequently Asked Questions

**🤔 What is a Standalone Word?**
A standalone word is a word that is not part of another word. For example, if you have the standalone word `fort` in your standalone filter list, the word `forts` will not be flagged.

**❓ What is a Phrase?**
A phrase refers to any text that consists of more than a single word. With Chat Control, you can target and filter out entire phrases to maintain the desired environment on your server.

**🔠 How do Replacement Letters Work?**
Replacement letters are letters that are substituted before the message is filtered. For example, if you set a replacement of `1` with `i`, any occurrence of the number one will be replaced with the letter i before the message is filtered. This feature enhances the accuracy of your filtering system.

**⚙️ I Already Have Another Mod with a Mute Command. How Can I Prevent Conflict?**
No worries! You can easily disable the mute command in the Chat Control configuration file located at `config/chatcontrol.json` in your server folder. Simply set the `muteCommand` option to false, and after restarting the server, you won't encounter any conflicts.

⚠️ Please note that Chat Control is currently available exclusively for Fabric. We do not have plans to create a Forge version at the moment.
