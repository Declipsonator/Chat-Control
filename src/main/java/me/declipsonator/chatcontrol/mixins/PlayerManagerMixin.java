package me.declipsonator.chatcontrol.mixins;

import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        if(sender == null || Config.isIgnored(sender.getUuid())) return;
        String string = message.getContent().getString();
        if(Config.isMuted(sender.getUuid())
                || (!Config.censorAndSend && (Config.checkWords(string)
                || Config.checkPhrases(string)
                || Config.checkRegexes(string)
                || Config.checkStandAloneWords(string)))) {
            ci.cancel();
            if(Config.logFiltered) {
                ChatControl.LOG.info("Filtered message from " + sender.getDisplayName().getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());
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

    @ModifyVariable(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private SignedMessage onBroadcastChatMessage(SignedMessage message, SignedMessage f, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender, MessageType.Parameters params) {
        if(!Config.isMuted(sender.getUuid()) && Config.censorAndSend && !Config.isIgnored(sender.getUuid())) {
            String newMessage = Config.censorWords(message.getContent().getString());
            newMessage = Config.censorPhrases(newMessage);
            newMessage = Config.censorRegexes(newMessage);
            newMessage = Config.censorStandAloneWords(newMessage);
            if(!newMessage.equals(message.getContent().getString())) {
                if(Config.tellPlayer) sender.sendMessage(Text.of("Your message was censored by Chat Control"));
                if(Config.logFiltered) ChatControl.LOG.info("Censored message from " + sender.getDisplayName().getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());

                return SignedMessage.ofUnsigned(newMessage);
//                return new SignedMessage(message.link(), message.signature(),
//                        new MessageBody(newMessage, message.signedBody().timestamp(), message.signedBody().salt(), message.signedBody().lastSeenMessages()),
//                        Text.of(newMessage), message.filterMask());
            }

        }
        return message;
    }

}
