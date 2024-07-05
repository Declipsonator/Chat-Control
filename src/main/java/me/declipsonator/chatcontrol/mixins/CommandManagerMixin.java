package me.declipsonator.chatcontrol.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.regex.Pattern;

@Mixin(value = CommandManager.class, priority = 500)
public class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method="execute", at = @At(value = "HEAD"), cancellable = true)
    private void onExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo cir) {
        ServerCommandSource source = parseResults.getContext().getSource();
        command = command.replaceFirst(Pattern.quote("/"), "");

        if(command.startsWith("say") || command.startsWith("me") || (!Config.ignorePrivateMessages && (command.startsWith("whisper") || command.startsWith("tell") || command.startsWith("msg") || command.startsWith("w")))) {
            String string = command.replaceFirst("say ", "");
            string = string.replaceFirst("me ", "");
            string = string.replaceFirst("w ", "");
            string = string.replaceFirst("tell ", "");
            string = string.replaceFirst("msg ", "");
            string = string.replaceFirst("w ", "");

            ServerPlayerEntity sender = source.getPlayer();
            if(sender == null || Config.isIgnored(sender.getUuid())) return;
            if(Config.isMuted(sender.getUuid())
                    || (!Config.censorAndSend && (Config.checkWords(string)
                    || Config.checkPhrases(string)
                    || Config.checkRegexes(string)
                    || Config.checkStandAloneWords(string)))) {
                cir.cancel();
                if(Config.logFiltered) {
                    ChatControl.LOG.info(Text.translatable("text.control.feedback.filteredMessageFrom").getString() + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + string);
                }
                if(Config.tellPlayer && Config.isTempMuted(sender.getUuid())) {
                    sender.sendMessage(Text.of(String.format(Text.translatable("text.control.feedback.moreMinutes").getString(), (Config.timeLeftTempMuted(sender.getUuid()) / 60000)) + Text.translatable("text.control.feedback.reason") + Config.getMuteReason(sender.getUuid())));
                } else if(Config.tellPlayer && Config.isMuted(sender.getUuid())) {
                    sender.sendMessage(Text.translatable("text.control.feedback.muted").append(Text.translatable("text.control.feedback.reason").getString() +  Config.getMuteReason(sender.getUuid())));
                } else if(Config.tellPlayer) {
                    sender.sendMessage(Text.translatable("text.control.feedback.filteredMessage"));

                    if(Config.muteAfterOffense) {
                        Config.addOffense(sender.getUuid());
                        if (Config.offenseCount(sender.getUuid()) >= Config.muteAfterOffenseNumber) {
                            if (Config.muteAfterOffenseType == Config.MuteType.PERMANENT) {
                                Config.addMutedPlayer(sender.getUuid(), Text.translatable("text.control.feedback.repeatedOffenses").getString());
                                sender.sendMessage(Text.translatable("text.control.feedback.permMuted"));
                            } else {
                                Config.addTempMutedPlayer(sender.getUuid(), System.currentTimeMillis() + (Config.muteAfterOffenseMinutes * 60000L), Text.translatable("text.control.feedback.repeatedOffenses").getString());
                                sender.sendMessage(Text.of(String.format(Text.translatable("text.control.feedback.tempMuted").getString(),  Config.muteAfterOffenseMinutes)));
                            }
                            Config.removeOffenses(sender.getUuid());
                        }
                    }
                }
            }

        }

    }

    @ModifyVariable(method = "execute", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private ParseResults<ServerCommandSource> onExecution(ParseResults<ServerCommandSource> parseResults,  ParseResults<ServerCommandSource> p, String command) {
        ServerCommandSource source = parseResults.getContext().getSource();
        command = command.replaceFirst(Pattern.quote("/"), "");
        if(command.startsWith("say") || command.startsWith("me") || (!Config.ignorePrivateMessages && (command.startsWith("whisper") || command.startsWith("tell") || command.startsWith("msg") || command.startsWith("w")))) {
            String string = command.replaceFirst("say ", "");
            string = string.replaceFirst("me ", "");
            string = string.replaceFirst("w ", "");
            string = string.replaceFirst("tell ", "");
            string = string.replaceFirst("msg ", "");
            string = string.replaceFirst("w ", "");
            ServerPlayerEntity sender = source.getPlayer();
            if (sender == null || Config.isIgnored(sender.getUuid())) return parseResults;
            if (!Config.isMuted(sender.getUuid()) && Config.censorAndSend) {
                String newMessage = Config.censorWords(string);
                newMessage = Config.censorPhrases(newMessage);
                newMessage = Config.censorRegexes(newMessage);
                newMessage = Config.censorStandAloneWords(newMessage);
                newMessage = Config.censorWords(newMessage);
                if (!newMessage.equals(string)) {
                    if(Config.muteAfterOffense) {
                        Config.addOffense(sender.getUuid());
                        if (Config.offenseCount(sender.getUuid()) >= Config.muteAfterOffenseNumber) {
                            if (Config.muteAfterOffenseType == Config.MuteType.PERMANENT) {
                                Config.addMutedPlayer(sender.getUuid(), Text.translatable("text.control.feedback.repeatedOffenses").getString());
                                sender.sendMessage(Text.translatable("text.control.feedback.permMuted"));
                            } else {
                                Config.addTempMutedPlayer(sender.getUuid(), System.currentTimeMillis() + (Config.muteAfterOffenseMinutes * 60000L), Text.translatable("text.control.feedback.repeatedOffenses").getString());
                                sender.sendMessage(Text.of(String.format(Text.translatable("text.control.feedback.tempMuted").getString(),  Config.muteAfterOffenseMinutes)));
                            }
                            Config.removeOffenses(sender.getUuid());
                        }
                    }
                    return dispatcher.parse(command.split(" ")[0] + " " +  newMessage, source);
                }
            }
        }
        return parseResults;
    }


    @ModifyVariable(method = "execute", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private String onExecute(String command, ParseResults<ServerCommandSource> parseResults, String c) {
        ServerCommandSource source = parseResults.getContext().getSource();
        String newCommand = command.replaceFirst(Pattern.quote("/"), "");

        if(newCommand.startsWith("say") || newCommand.startsWith("me") || (!Config.ignorePrivateMessages && (newCommand.startsWith("whisper") || newCommand.startsWith("tell") || newCommand.startsWith("msg") || newCommand.startsWith("w")))) {

            String string = newCommand.replaceFirst("say ", "");
            string = string.replaceFirst("me ", "");
            string = string.replaceFirst("w ", "");
            string = string.replaceFirst("tell ", "");
            string = string.replaceFirst("msg ", "");
            string = string.replaceFirst("w ", "");

            ServerPlayerEntity sender = source.getPlayer();
            if (sender == null || Config.isIgnored(sender.getUuid())) return command;
            if (!Config.isMuted(sender.getUuid()) && Config.censorAndSend) {
                String newMessage = Config.censorWords(string);
                newMessage = Config.censorPhrases(newMessage);
                newMessage = Config.censorRegexes(newMessage);
                newMessage = Config.censorStandAloneWords(newMessage);
                newMessage = Config.censorWords(newMessage);
                if (!newMessage.equals(string)) {

                    if(Config.tellPlayer) sender.sendMessage(Text.translatable("text.control.feedback.censoredMessage"));
                    if (Config.logFiltered)
                        ChatControl.LOG.info(Text.translatable("text.control.feedback.censoredMessageFrom").getString() + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + string);
                    if(Config.muteAfterOffense) {
                        Config.addOffense(sender.getUuid());
                        if (Config.offenseCount(sender.getUuid()) >= Config.muteAfterOffenseNumber) {
                            if (Config.muteAfterOffenseType == Config.MuteType.PERMANENT) {
                                Config.addMutedPlayer(sender.getUuid(), Text.translatable("text.control.feedback.repeatedOffenses").getString());

                                sender.sendMessage(Text.translatable("text.control.feedback.permMuted"));
                            } else {
                                Config.addTempMutedPlayer(sender.getUuid(), System.currentTimeMillis() + (Config.muteAfterOffenseMinutes * 60000L), Text.translatable("text.control.feedback.repeatedOffenses").getString());
                                sender.sendMessage(Text.of(String.format(Text.translatable("text.control.feedback.tempMuted").getString(),  Config.muteAfterOffenseMinutes)));
                            }
                            Config.removeOffenses(sender.getUuid());
                        }
                    }
                    return command.split(" ")[0] + " " +  newMessage;
                }
            }
        }
        return command;
    }




}
