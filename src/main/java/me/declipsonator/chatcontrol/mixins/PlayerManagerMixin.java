package me.declipsonator.chatcontrol.mixins;

import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        ChatControl.LOG.info(message.getContent().getString());
        if(message.getContent().getString().startsWith("/") && Config.ignoreCommands) return;
        if(sender == null || Config.isIgnored(sender.getUuid())) return;
        String string = message.getContent().getString();
        if(Config.isMuted(sender.getUuid())
                || Config.checkWords(string)
                || Config.checkPhrases(string)
                || Config.checkRegexes(string)) {
            ci.cancel();
            if(Config.logFiltered) {
                ChatControl.LOG.info("Filtered message from " + sender.getDisplayName().getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());
            }
        }
    }
}
