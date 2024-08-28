package me.declipsonator.chatcontrol.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.declipsonator.chatcontrol.util.Config;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import me.declipsonator.chatcontrol.util.Offense;
import me.declipsonator.chatcontrol.util.PlayerUtils;
import me.declipsonator.chatcontrol.util.ReplacementChar;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FilterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("filter").requires(source -> Permissions.check(source, "chatcontrol.filter", 1))
                .then(literal("add")
                        .then(literal("word").then(argument("to_block", StringArgumentType.word()).executes(context -> {
                            if(Config.isWord(context.getArgument("to_block", String.class))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.wordAlreadyBlocked"));
                                return 0;
                            }
                            Config.addWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.wordAdded"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("standAloneWord").then(argument("to_block", StringArgumentType.word()).executes(context -> {
                            if(Config.isStandAloneWord(context.getArgument("to_block", String.class))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.standAloneWordAlreadyBlocked"));
                                return 0;
                            }
                            Config.addStandAloneWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.standAloneAdded"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("phrase").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(Config.isPhrase(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.phraseAlreadyBlocked"));
                                return 0;
                            }
                            Config.addPhrase(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.phraseAdded"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("regex").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(Config.isRegex(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.regexAlreadyBlocked"));
                                return 0;
                            }
                            Config.addRegex(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.regexAdded"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                )
                .then(literal("remove").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("word").then(argument("to_block", StringArgumentType.word()).suggests((context, builder) -> CommandSource.suggestMatching(Config.getWords(), builder)).executes(context -> {
                            if(!Config.isWord(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.wordNotFound"));
                                return 0;
                            }
                            Config.removeWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.wordRemoved"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("standAloneWord").then(argument("to_block", StringArgumentType.word()).suggests((context, builder) -> CommandSource.suggestMatching(Config.getStandAloneWords(), builder)).executes(context -> {
                            if(!Config.isStandAloneWord(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.standAloneWordNotFound"));
                                return 0;
                            }
                            Config.removeStandAloneWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.standAloneWordRemoved"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("phrase").then(argument("to_block", StringArgumentType.greedyString()).suggests((context, builder) -> CommandSource.suggestMatching(Config.getPhrases(), builder)).executes(context -> {
                            if(!Config.isPhrase(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.phraseNotFound"));
                                return 0;
                            }
                            Config.removePhrase(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.phraseRemoved"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("regex").then(argument("to_block", StringArgumentType.greedyString()).suggests((context, builder) -> CommandSource.suggestMatching(Config.getRegexes(), builder)).executes(context -> {
                            if(!Config.isRegex(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.regexNotFound"));
                                return 0;
                            }
                            Config.removeRegex(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.regexRemoved"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                )
                .then(literal("list").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("words").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredWords").append(Config.getWords().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("standAloneWords").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredStandAloneWords").append(Config.getStandAloneWords().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("phrases").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredPhrases").append(Config.getPhrases().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("regexes").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredRegexes").append(Config.getRegexes().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("all").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredWords").append(Config.getWords().toString()), false);
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredStandAloneWords").append(Config.getStandAloneWords().toString()), false);
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredPhrases").append(Config.getPhrases().toString()), false);
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.filteredRegexes").append(Config.getRegexes().toString()), false);

                            return SINGLE_SUCCESS;
                        }))
                )
                .then(literal("config").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("logFiltered").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.loggingFiltered").append(String.valueOf(Config.logFiltered)), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.logFiltered = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.loggingFiltered").append(String.valueOf(Config.logFiltered)), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("tellPlayer").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.tellingPlayer").append(String.valueOf(Config.tellPlayer)), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.tellPlayer = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.tellingPlayer").append(String.valueOf(Config.tellPlayer)), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("censorAndSend").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.censoring").append(String.valueOf(Config.censorAndSend)), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.censorAndSend = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.censoring").append(String.valueOf(Config.censorAndSend)), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("ignorePrivateMessages").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.ignoringPrivateMessages").append(String.valueOf(Config.ignorePrivateMessages)), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.ignorePrivateMessages = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.ignoringPrivateMessages").append(String.valueOf(Config.ignorePrivateMessages)), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("reload").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.reloadingConfig"), true);
                            Config.loadConfig();
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("save").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.savingConfig"), true);
                            Config.saveConfig();
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("caseSensitive").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.caseSensitive").append(String.valueOf(Config.caseSensitive)), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.caseSensitive = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.caseSensitive").append(String.valueOf(Config.caseSensitive)), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("replacementLetters").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.replacementLetters").append(Config.getReplacementChars().toString()), false);
                            return SINGLE_SUCCESS;
                        })
                        .then(literal("add").then(argument("string", StringArgumentType.greedyString()).executes(context -> {
                            String replacements = StringArgumentType.getString(context, "string");
                            String[] split = replacements.split(" ");
                            if(replacements.length() != 3 || split.length != 2) {
                                context.getSource().sendError(Text.translatable("text.control.filter.invalidSyntax"));
                                return 0;
                            }

                            if(Config.isReplacementChar(new ReplacementChar(split[0].charAt(0), split[1].charAt(0)))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.letterAlreadyInList"));
                                return 0;
                            }

                            Config.addReplacementChar(split[0].charAt(0), split[1].charAt(0));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.replacementAdded"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("remove").then(argument("string", StringArgumentType.greedyString()).suggests((context, builder) -> {
                            List<String> suggestions = new ArrayList<>();
                            for (ReplacementChar replacementChar : Config.getReplacementChars()) {
                                suggestions.add(replacementChar.toReplace + " " + replacementChar.replaceWith);
                            }
                            return CommandSource.suggestMatching(suggestions, builder);
                        }).executes(context -> {
                            String replacements = StringArgumentType.getString(context, "string");
                            String[] split = replacements.split(" ");
                            if(replacements.length() != 3 || split.length != 2) {
                                context.getSource().sendError(Text.translatable("text.control.filter.invalidReplacementSyntax"));
                                return 0;
                            }

                            if(!Config.isReplacementChar(new ReplacementChar(split[0].charAt(0), split[1].charAt(0)))) {
                                context.getSource().sendError(Text.translatable("text.control.filter.letterNotInList"));
                                return 0;
                            }

                            Config.removeReplacementChar(split[0].charAt(0), split[1].charAt(0));
                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.replacementRemoved"), false);

                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        }))))
                        .then(literal("ignoredPlayers").executes(context -> {
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.ignoredPlayers").append(Config.getIgnoredPlayers().toString()), false);
                                    return SINGLE_SUCCESS;
                                })
                                .then(literal("add").then(argument("player", GameProfileArgumentType.gameProfile()).executes(context -> {
                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                                        if (Config.isIgnored(profile.getId())) continue;
                                        Config.addIgnoredPlayer(profile.getId());
                                        context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.filter.nowIgnored")), false);
                                    }
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("remove").then(argument("player", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
                                    List<UUID> suggestions = Config.getIgnoredPlayers();
                                    List<String> stringSuggestions = PlayerUtils.getPlayerNames(suggestions);

                                    return CommandSource.suggestMatching(stringSuggestions, builder);
                                }).executes(context -> {
                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                                        if (!Config.isIgnored(profile.getId())) continue;
                                        Config.removeIgnoredPlayer(profile.getId());
                                        context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.filter.notIgnored")), false);
                                    }
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                        )
                        .then(literal("muteAfterOffense").executes(context -> {
                                    if(!Config.muteAfterOffense) {
                                        context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffense").append(String.valueOf(Config.muteAfterOffense)), false);
                                    } else {
                                        if(Config.muteAfterOffenseType == Config.MuteType.PERMANENT) {
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffense").append(String.valueOf(Config.muteAfterOffense)), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterNumOffenses").append(String.valueOf(Config.muteAfterOffenseNumber)), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.offenseExpireMinutes").append(String.valueOf(Config.offenseExpireMinutes)), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.permanentMute"), false);
                                        } else {
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffense").append(String.valueOf(Config.muteAfterOffense)), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterNumOffenses").append(String.valueOf(Config.muteAfterOffenseNumber)), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.offenseExpireMinutes").append(String.valueOf(Config.offenseExpireMinutes)), false);
                                            context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.tempMute").append(String.valueOf(Config.muteAfterOffenseMinutes)), false);
                                        }

                                    }
                                    return SINGLE_SUCCESS;
                                })
                                .then(literal("set").then(argument("value", BoolArgumentType.bool()).executes(context -> {
                                    Config.muteAfterOffense = BoolArgumentType.getBool(context, "value");
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffense").append(String.valueOf(Config.muteAfterOffense)), false);
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("type").then(argument("type", StringArgumentType.word()).suggests((context, builder) -> {
                                    List<String> suggestions = new ArrayList<>();
                                    for(Config.MuteType type : Config.MuteType.values()) {
                                        suggestions.add(type.name());
                                    }
                                    return CommandSource.suggestMatching(suggestions, builder);
                                }).executes(context -> {
                                    Config.muteAfterOffenseType = Config.MuteType.valueOf(StringArgumentType.getString(context, "type").toUpperCase());
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffenseType").append(String.valueOf(Config.muteAfterOffenseType)), true);
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("number").then(argument("number", IntegerArgumentType.integer(0)).executes(context -> {
                                    Config.muteAfterOffenseNumber = IntegerArgumentType.getInteger(context, "number");
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffenseNumber").append(String.valueOf(Config.muteAfterOffenseNumber)), true);
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("length").then(argument("minutes", IntegerArgumentType.integer(0)).executes(context -> {
                                    Config.muteAfterOffenseMinutes = IntegerArgumentType.getInteger(context, "minutes");
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.muteAfterOffenseMinutes").append(String.valueOf(Config.muteAfterOffenseMinutes)), true);
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("expireMinutes").then(argument("minutes", IntegerArgumentType.integer(0)).executes(context -> {
                                    Config.offenseExpireMinutes = IntegerArgumentType.getInteger(context, "minutes");
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.offenseExpireMinutes").append(String.valueOf(Config.offenseExpireMinutes)), true);
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("currentOffenses").executes(context -> {
                                    StringBuilder offenses = new StringBuilder("[");
                                    for(Offense offense : Config.offenses) {
                                        UUID uuid = offense.uuid();
                                        offenses.append(PlayerUtils.getPlayerName(String.valueOf(uuid))).append(": ").append(Config.offenseCount(uuid)).append(" offenses,");
                                    }
                                    offenses = new StringBuilder(offenses.substring(0, offenses.length() - 1) + "]");
                                    String finalOffenses = offenses.toString();
                                    context.getSource().sendFeedback(() -> Text.translatable("text.control.filter.offenses").append(finalOffenses), false);
                                    return SINGLE_SUCCESS;
                                }))
                        )

                )
        );
    }
}
