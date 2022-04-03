package me.declipsonator.chatcontrol.command;


import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MuteCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {


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
                                        context.getSource().sendFeedback(Text.of("Permanent mute added"), true);
                                    }
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
                                                context.getSource().sendFeedback(Text.of("Temporary mute added"), true);
                                            }
                                            return SINGLE_SUCCESS;
                                        }))))

                )
                .then(literal("remove").then(argument("player", GameProfileArgumentType.gameProfile()).executes(context -> {
                    for(GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "player")) {
                        if (!Config.isMuted(profile.getId())) {
                            context.getSource().sendError(Text.of(profile.getName() + " is not muted!"));
                        }

                        Config.removeMutedPlayer(profile.getId());
                        context.getSource().sendFeedback(Text.of(profile.getName() + " has been unmuted"), true);
                    }
                    return SINGLE_SUCCESS;
                })))
                .then(literal("list").executes(context -> {
                    context.getSource().sendFeedback(Text.of("Muted Players: " + Config.getMutedPlayers().toString()), false);
                    context.getSource().sendFeedback(Text.of("Temporary Muted Players: " + Config.getTempMutedPlayers().toString()), false);
                    return SINGLE_SUCCESS;
                }))
        );


    }
}
