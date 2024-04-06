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
    private void onExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
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
                ci.cancel();
                if(Config.logFiltered) {
                    ChatControl.LOG.info("Filtered message from " + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + string);
                }
                if(Config.tellPlayer && Config.isTempMuted(sender.getUuid())) {
                    sender.sendMessage(Text.of("You are muted for " + (Config.timeLeftTempMuted(sender.getUuid()) / 60000) + " more minutes"));
                } else if(Config.tellPlayer && Config.isMuted(sender.getUuid())) {
                    sender.sendMessage(Text.of("You are muted"));
                } else if(Config.tellPlayer) {
                    sender.sendMessage(Text.of("Your message was filtered by Chat Control. Please refrain from using that language."));
                }
            }

        }

    }

    @ModifyVariable(method = "execute", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private ParseResults<ServerCommandSource> onExecution(ParseResults<ServerCommandSource> parseResults,  ParseResults<ServerCommandSource> p, String command) {
        ServerCommandSource source = parseResults.getContext().getSource();
        command = command.replaceFirst(Pattern.quote("/"), "");
        System.out.println("I RAN");
        System.out.println(command);
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
                    return dispatcher.parse(command.split(" ")[0] + " " +  newMessage, source);
                }
            }
        }
        return parseResults;
    }


    @ModifyVariable(method = "execute", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private String onExecute(String command, ParseResults<ServerCommandSource> parseResults, String c) {
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
            if (sender == null || Config.isIgnored(sender.getUuid())) return command;
            if (!Config.isMuted(sender.getUuid()) && Config.censorAndSend) {
                String newMessage = Config.censorWords(string);
                newMessage = Config.censorPhrases(newMessage);
                newMessage = Config.censorRegexes(newMessage);
                newMessage = Config.censorStandAloneWords(newMessage);
                newMessage = Config.censorWords(newMessage);
                if (!newMessage.equals(string)) {
                    if (Config.tellPlayer) sender.sendMessage(Text.of("Your message was censored by Chat Control"));
                    if (Config.logFiltered)
                        ChatControl.LOG.info("Censored message from " + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + string);
                    return command.split(" ")[0] + " " +  newMessage;
                }
            }
        }
        return command;
    }




}
