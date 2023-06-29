# Chat Control

Protect your server and the people on it with a system of filtering and muting.\
\
<b>Highly Reccommended to set `enforce-secure-profile` to false in your server.properties to protect the users in the server and prevent any issues with the mod.</b>



### Features:

- Filter by word
- Filter by standalone Word
- Filter by phrase
- Filter by regex
- Replace letters before filtering (ex. f1rst -> first)
- Censor message but stills send them option
- Permanently mute players
- Temporarily mute players
- Report filtered messages to log option
- Ignore privatemessages option
- Case Sensitive option


### Command Tutorial:

```
Add filter:
/filter add {word|standAloneWord|phrase|regex} {text}

Remove filter:
/filter remove {word||standAloneWord|phrase|regex} {text}

View filters:
/filter list {all|word||standAloneWord|phrase|regex}
 
Add replacement letters:
/filter config replacementLetters add {letter to replace} {letter to replace with}

Remove replacement letters:
/filter config replacementLetters remove {letter to replace} {letter to replace with}

Add ignored players:
/filter config ignoredPlayers add {player}

Remove ignored players:
/filter config ignoredPlayers remove {player}

Mute player:
/mute add permanent {player}

Temporarily mute player:
/mute add temporary {player} {minutes}

Remove a mute:
/mute remove {player}

Change Setting:
/filter config {logFiltered|ignorePrivateMessages|caseSensitive|tellPlayer|censorAndSend} {true|false}
```
## Info

### FAQ
<b>What is a standalone word?</b>\
A standalone word is a word that is not part of another word. For example, the word `forts` will not be flagged if you have the standalone word `fort` in your standalone filter list.\
\
<b>What is a phrase?</b>\
A phrase is anything more than a single word.\
\
<b>How do replacement letters work?</b>\
Replacement letters are letters that are replaced before the message is filtered. 
For example, if your have the replacement of `1` with `i`, any ones will be replaced with i's before the message is filtered.\
\
<b>I already have another mod that has the mute command, how can I prevent conflict?</b>\
You can disable the mute command in the config file. The config file is found in your server folder under `config/chatcontrol.json`. Just set `muteCommand` to false and restart the server.

⚠️This is a Fabric only mod and there are no plans to make a forge version.
