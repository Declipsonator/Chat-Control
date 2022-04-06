package me.declipsonator.chatcontrol.mixins;

import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(Text message, MessageType type, UUID sender, CallbackInfo ci) {
        if(message.getString().startsWith("/") && Config.ignoreCommands) return;
        if(sender == null || Config.isIgnored(sender)) return;
        StringBuilder sb = new StringBuilder(message.getString());
        sb.delete(0, sb.indexOf("]") + 2);
        String adjustedMessage = sb.toString();
        if(Config.isMuted(sender)
        || Config.checkWords(adjustedMessage)
        || Config.checkPhrases(adjustedMessage)
        || Config.checkRegexes(adjustedMessage)) {
            ci.cancel();
            if(Config.logFiltered) {
                ChatControl.LOG.info("Filtered message from " + sender + ": " + message.getString());
            }

        }
    }

    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(Text serverMessage, Function<ServerPlayerEntity, Text> playerMessageFactory, MessageType type, UUID sender, CallbackInfo ci) {
        if(serverMessage.getString().startsWith("/") && Config.ignoreCommands) return;
        if(sender == null || Config.isIgnored(sender)) return;
        StringBuilder sb = new StringBuilder(serverMessage.getString());
        sb.delete(0, sb.indexOf(">") + 2);
        String adjustedMessage = sb.toString();
        if(Config.isMuted(sender)
                || Config.checkWords(adjustedMessage)
                || Config.checkPhrases(adjustedMessage)
                || Config.checkRegexes(adjustedMessage)) {
            ci.cancel();
            if(Config.logFiltered) {
                ChatControl.LOG.info("Filtered message from " + sender + ": " + serverMessage.getString());
            }
        }
    }
}
