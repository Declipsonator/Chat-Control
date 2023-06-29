package me.declipsonator.chatcontrol.command;


import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.declipsonator.chatcontrol.util.Config;
import me.declipsonator.chatcontrol.util.PlayerUtils;
import me.declipsonator.chatcontrol.util.TempMutedPlayer;
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

        dispatcher.register(literal("mute").requires(source -> source.hasPermissionLevel(4))
                .then(literal("add")
                        .then(literal("permanent")
                                .then(argument("target", GameProfileArgumentType.gameProfile()).executes(context -> {
                                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "target")) {
                                        if (Config.isMuted(profile.getId())) {
                                            context.getSource().sendError(Text.of(profile.getName() + " is already muted."));
                                            return 0;
                                        }

                                        Config.addMutedPlayer(profile.getId());
                                        context.getSource().sendFeedback(() -> Text.of(profile.getName() + " has been permanently muted"), true);
                                    }
                                    Config.saveConfig();
                                    return SINGLE_SUCCESS;
                                })))
                        .then(literal("temporary")
                                .then(argument("target", GameProfileArgumentType.gameProfile())
                                        .then(argument("minutes", IntegerArgumentType.integer(0, 525960)).executes(context -> {
                                            for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "target")) {
                                                if (Config.isMuted(profile.getId())) {
                                                    context.getSource().sendError(Text.of(profile.getName() + " is already muted!"));
                                                }
                                                long until = System.currentTimeMillis() + (IntegerArgumentType.getInteger(context, "minutes") * 60000L);
                                                Config.addTempMutedPlayer(profile.getId(), until);
                                                context.getSource().sendFeedback(() -> Text.of(profile.getName() + " has been temporarily muted"), true);
                                            }
                                            Config.saveConfig();
                                            return SINGLE_SUCCESS;
                                        }))))

                )
                .then(literal("remove").then(argument("player", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
                    List<TempMutedPlayer> tempMutedPlayers = Config.getTempMutedPlayers();
                    List<UUID> mutedPlayers = Config.getMutedPlayers();
                    tempMutedPlayers.forEach(player -> mutedPlayers.add(player.uuid()));
                    return CommandSource.suggestMatching(PlayerUtils.getPlayerNames(mutedPlayers), builder);
                }).executes(context -> {
                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                        if (!Config.isMuted(profile.getId())) {
                            context.getSource().sendError(Text.of(profile.getName() + " is not muted!"));
                            return SINGLE_SUCCESS;
                        }

                        Config.removeMutedPlayer(profile.getId());
                        context.getSource().sendFeedback(() -> Text.of(profile.getName() + " has been unmuted"), true);
                    }
                    Config.saveConfig();
                    return SINGLE_SUCCESS;
                })))
                .then(literal("list").executes(context -> {
                    context.getSource().sendFeedback(() -> Text.of("Muted Players: " + PlayerUtils.getPlayerNames(Config.getMutedPlayers())), false);
                    List<UUID> tempPlayers = new ArrayList<>();
                    Config.getTempMutedPlayers().forEach(player -> tempPlayers.add(player.uuid()));
                    context.getSource().sendFeedback(() -> Text.of("Temporary Muted Players: " + PlayerUtils.getPlayerNames(tempPlayers)), false);
                    return SINGLE_SUCCESS;
                }))
        );


    }
}
