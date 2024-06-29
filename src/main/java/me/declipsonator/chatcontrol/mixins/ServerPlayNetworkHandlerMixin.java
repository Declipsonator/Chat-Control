package me.declipsonator.chatcontrol.mixins;

import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 500)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method="handleDecoratedMessage", at = @At(value = "HEAD"), cancellable = true)
    private void onHandleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
        ServerPlayerEntity sender = ((ServerPlayNetworkHandler) (Object) this).player;

        if(sender == null || Config.isIgnored(sender.getUuid())) return;
        String string = message.getContent().getString();
        if(Config.isMuted(sender.getUuid())
                || (!Config.censorAndSend && (Config.checkWords(string)
                || Config.checkPhrases(string)
                || Config.checkRegexes(string)
                || Config.checkStandAloneWords(string)))) {
            ci.cancel();
            if(Config.logFiltered) {
                ChatControl.LOG.info(Text.translatable("text.control.feedback.filteredMessageFrom").getString() +  Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());
            }
            if(Config.tellPlayer && Config.isTempMuted(sender.getUuid())) {
                sender.sendMessage(Text.of(String.format(Text.translatable("text.control.feedback.moreMinutes").getString(), (Config.timeLeftTempMuted(sender.getUuid()) / 60000)) + Text.translatable("text.control.feedback.reason").getString() +  Config.getMuteReason(sender.getUuid())));
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

    @ModifyVariable(method = "handleDecoratedMessage", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private SignedMessage onHandleDecoratedMessage(SignedMessage message, SignedMessage m) {
        ServerPlayerEntity sender = ((ServerPlayNetworkHandler) (Object) this).player;
        if(Config.ignorePrivateMessages || Config.isIgnored(sender.getUuid())) return message;
        if(!Config.isMuted(sender.getUuid()) && Config.censorAndSend) {
            String newMessage = Config.censorWords(message.getContent().getString());
            newMessage = Config.censorPhrases(newMessage);
            newMessage = Config.censorRegexes(newMessage);
            newMessage = Config.censorStandAloneWords(newMessage);
            newMessage = Config.censorWords(newMessage);
            if(!newMessage.equals(message.getContent().getString())) {
                if(Config.tellPlayer) sender.sendMessage(Text.translatable("text.control.feedback.censoredMessage"));
                if(Config.logFiltered) ChatControl.LOG.info(Text.translatable("text.control.feedback.censoredMessageFrom").getString() +  Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());

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
                return SignedMessage.ofUnsigned(newMessage);
            }
        }
        return message;
    }


}
