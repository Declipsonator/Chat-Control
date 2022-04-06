package me.declipsonator.chatcontrol.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.declipsonator.chatcontrol.util.CharArgumentType;
import me.declipsonator.chatcontrol.util.Config;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import me.declipsonator.chatcontrol.util.ReplacementChar;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;


public class FilterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("filter").requires(source -> source.hasPermissionLevel(4))
                .then(literal("add")
                        .then(literal("word").then(argument("to_block", StringArgumentType.word()).executes(context -> {
                            if(Config.isWord(context.getArgument("to_block", String.class))) {
                                context.getSource().sendError(Text.of("Word already Blocked"));
                                return 0;
                            }
                            Config.addWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(Text.of("Word added to filter"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("phrase").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(Config.isPhrase(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.of("Phrase already Blocked"));
                                return 0;
                            }
                            Config.addPhrase(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(Text.of("Phrase added to filter"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("regex").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(Config.isRegex(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.of("Regex already Blocked"));
                                return 0;
                            }
                            Config.addRegex(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(Text.of("Regex added to filter"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                )
                .then(literal("remove").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("word").then(argument("to_block", StringArgumentType.word()).executes(context -> {
                            if(!Config.isWord(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.of("Word not found"));
                                return 0;
                            }
                            Config.removeWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(Text.of("Word removed from filter"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("phrase").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(!Config.isPhrase(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.of("Phrase not found"));
                                return 0;
                            }
                            Config.removePhrase(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(Text.of("Phrase removed from filter"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("regex").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(!Config.isRegex(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendError(Text.of("Regex not found"));
                                return 0;
                            }
                            Config.removeRegex(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendFeedback(Text.of("Regex removed from filter"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                )
                .then(literal("list").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("words").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Filtered Words: " + Config.getWords().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("phrases").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Filtered Phrases: " + Config.getPhrases().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("regexes").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Filtered Regexes: " + Config.getRegexes().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("all").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Filtered Words: " + Config.getWords().toString()), false);
                            context.getSource().sendFeedback(Text.of("Filtered Phrases: " + Config.getPhrases().toString()), false);
                            context.getSource().sendFeedback(Text.of("Filtered Regexes: " + Config.getRegexes().toString()), false);

                            return SINGLE_SUCCESS;
                        }))
                )
                .then(literal("config").requires(source -> source.hasPermissionLevel(4))
                        .then(literal("logFiltered").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Logging filtered messages: " + Config.logFiltered), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.logFiltered = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(Text.of("Logging filtered messages: " + Config.logFiltered), true);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("ignoreCommands").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Ignoring commands: " + Config.ignoreCommands), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.ignoreCommands = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(Text.of("Ignoring commands: " + Config.ignoreCommands), true);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("reload").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Reloading chat control config."), true);
                            Config.loadConfig();
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("save").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Saving chat control config."), true);
                            Config.saveConfig();
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("caseSensitive").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Case Sensitive: " + Config.caseSensitive), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            Config.caseSensitive = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendFeedback(Text.of("Case Sensitive: " + Config.caseSensitive), true);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("replacementLetters").executes(context -> {
                            context.getSource().sendFeedback(Text.of("Replacement Letters: " + Config.getReplacementChars()), false);
                            return SINGLE_SUCCESS;
                        })
                        .then(literal("add").then(argument("to_replace", CharArgumentType.character()).then(argument("replace_with", CharArgumentType.character()).executes(context -> {

                            if(Config.isReplacementChar(new ReplacementChar(context.getArgument("to_replace", Character.class), context.getArgument("replace_with", Character.class)))) {
                                context.getSource().sendError(Text.of("Letter already in list"));
                                return 0;
                            }

                            Config.addReplacementChar(context.getArgument("to_replace", Character.class), context.getArgument("replace_with", Character.class));
                            context.getSource().sendFeedback(Text.of("Replacement added to config"), false);
                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        }))))
                        .then(literal("remove").then(argument("to_replace", CharArgumentType.character()).then(argument("replace_with",  CharArgumentType.character()).executes(context -> {
                            if(!Config.isReplacementChar(new ReplacementChar(context.getArgument("to_replace", Character.class), context.getArgument("replace_with", Character.class)))) {
                                context.getSource().sendError(Text.of("Letter not in list"));
                                return 0;
                            }

                            Config.removeReplacementChar(context.getArgument("to_replace", Character.class), context.getArgument("replace_with", Character.class));
                            context.getSource().sendFeedback(Text.of("Replacement removed from config"), false);

                            Config.saveConfig();

                            return SINGLE_SUCCESS;
                        })))))
                        .then(literal("ignoredPlayers").executes(context -> {
                                    context.getSource().sendFeedback(Text.of("Ignored Players: " + Config.getIgnoredPlayers()), false);
                                    return SINGLE_SUCCESS;
                                })
                                .then(literal("add").then(argument("player", GameProfileArgumentType.gameProfile()).executes(context -> {
                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                                        Config.addIgnoredPlayer(profile.getId());
                                        context.getSource().sendFeedback(Text.of(profile.getName() + " is now ignored"), false);
                                    }
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("remove").then(argument("player", GameProfileArgumentType.gameProfile()).executes(context -> {
                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                                        Config.removeIgnoredPlayer(profile.getId());
                                        context.getSource().sendFeedback(Text.of(profile.getName() + " is no longer ignored"), false);
                                    }
                                    Config.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                        )

                )
        );
    }
}
