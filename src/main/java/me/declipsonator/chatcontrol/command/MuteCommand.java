package me.declipsonator.chatcontrol.command;


import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.declipsonator.chatcontrol.util.Config;
import me.declipsonator.chatcontrol.util.MutedPlayer;
import me.declipsonator.chatcontrol.util.PlayerUtils;
import me.declipsonator.chatcontrol.util.TempMutedPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MuteCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if(!Config.muteCommand) return;
        dispatcher.register(literal("mute").requires(source -> Permissions.check(source, "chatcontrol.mute", 1))
                .then(literal("add")
                        .then(literal("permanent")
                                .then(argument("target", GameProfileArgumentType.gameProfile()).executes(context -> {
                                            for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "target")) {
                                                if (Config.isMuted(profile.getId())) {
                                                    context.getSource().sendError(Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.alreadyMuted")));
                                                    return 0;
                                                }

                                                Config.addMutedPlayer(profile.getId(), "No reason provided");
                                                context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.permanentlyMuted")), true);
                                            }
                                            Config.saveConfig();
                                            return SINGLE_SUCCESS;
                                        })
                                        .then(argument("reason", StringArgumentType.greedyString()).executes(context -> {
                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "target")) {
                                        if (Config.isMuted(profile.getId())) {
                                            context.getSource().sendError(Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.alreadyMuted")));
                                            return 0;
                                        }

                                        Config.addMutedPlayer(profile.getId(), context.getArgument("reason", String.class));
                                        context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.permanentlyMuted")), true);
                                    }
                                    Config.saveConfig();
                                    return SINGLE_SUCCESS;
                                }))))
                        .then(literal("temporary")
                                .then(argument("target", GameProfileArgumentType.gameProfile())
                                        .then(argument("minutes", IntegerArgumentType.integer(0, 525960)).executes(context -> {
                                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "target")) {
                                                        if (Config.isMuted(profile.getId())) {
                                                            context.getSource().sendError(Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.alreadyMuted")));
                                                        }
                                                        long until = System.currentTimeMillis() + (IntegerArgumentType.getInteger(context, "minutes") * 60000L);
                                                        Config.addTempMutedPlayer(profile.getId(), until, "No reason provided");
                                                        context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.temporarilyMuted")), true);
                                                    }
                                                    Config.saveConfig();
                                                    return SINGLE_SUCCESS;
                                                })
                                                .then(argument("reason", StringArgumentType.greedyString()).executes(context -> {
                                                        for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "target")) {
                                                            if (Config.isMuted(profile.getId())) {
                                                                context.getSource().sendError(Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.alreadyMuted")));
                                                            }
                                                            long until = System.currentTimeMillis() + (IntegerArgumentType.getInteger(context, "minutes") * 60000L);
                                                            Config.addTempMutedPlayer(profile.getId(), until, context.getArgument("reason", String.class));
                                                            context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.temporarilyMuted")), true);
                                                        }
                                                        Config.saveConfig();
                                                        return SINGLE_SUCCESS;
                                        })))
                                )
                        )

                )
                .then(literal("remove").then(argument("player", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
                    List<TempMutedPlayer> tempMutedPlayers = Config.getTempMutedPlayers();
                    List<MutedPlayer> mutedPlayers = Config.getMutedPlayers();
                    List<UUID> playersOnMuteList = new ArrayList<>();
                    tempMutedPlayers.forEach(player -> playersOnMuteList.add(player.uuid()));
                    mutedPlayers.forEach(player -> playersOnMuteList.add(player.uuid()));
                    return CommandSource.suggestMatching(PlayerUtils.getPlayerNames(playersOnMuteList), builder);
                }).executes(context -> {
                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                        if (!Config.isMuted(profile.getId())) {
                            context.getSource().sendError(Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.notMuted")));
                            return SINGLE_SUCCESS;
                        }

                        Config.removeMutedPlayer(profile.getId());
                        context.getSource().sendFeedback(() -> Text.of(profile.getName()).copy().append(Text.translatable("text.control.mute.unmuted")), true);

                    }
                    Config.saveConfig();
                    return SINGLE_SUCCESS;
                })))
                .then(literal("list").executes(context -> {
                    List<MutedPlayer> mutedPlayers = Config.getMutedPlayers();
                    List<TempMutedPlayer> tempMutedPlayers = Config.getTempMutedPlayers();
                    context.getSource().sendFeedback(() -> Text.translatable("text.control.mute.mutedPlayers"), false);
                    mutedPlayers.forEach(player -> context.getSource().sendFeedback(() -> Text.of(PlayerUtils.getPlayerName(player.uuid().toString()) + " - " + player.reason()), false));
                    context.getSource().sendFeedback(() -> Text.translatable("text.control.mute.tempMutedPlayers"), false);
                    tempMutedPlayers.forEach(player -> context.getSource().sendFeedback(() -> Text.of(PlayerUtils.getPlayerName(player.uuid().toString()) + " - " + player.reason()), false));
                    return SINGLE_SUCCESS;
                }))
        );


    }
}
