package me.declipsonator.chatcontrol.mixins;

import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.network.message.MessageSender;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Ljava/util/function/Function;Lnet/minecraft/network/message/MessageSender;Lnet/minecraft/util/registry/RegistryKey;)V", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(SignedMessage message, Function<ServerPlayerEntity, SignedMessage> playerMessageFactory, MessageSender sender, RegistryKey<MessageType> typeKey, CallbackInfo ci) {
        ChatControl.LOG.info(message.getContent().getString());
        if(message.getContent().getString().startsWith("/") && Config.ignoreCommands) return;
        if(sender == null || Config.isIgnored(sender.uuid())) return;
        String string = message.getContent().getString();
        if(Config.isMuted(sender.uuid())
                || Config.checkWords(string)
                || Config.checkPhrases(string)
                || Config.checkRegexes(string)) {
            ci.cancel();
            if(Config.logFiltered) {
                ChatControl.LOG.info("Filtered message from " + sender.name().getString() + " (" + sender.uuid().toString() + ")" + ": " + message.getContent().getString());
            }
        }
    }
}
