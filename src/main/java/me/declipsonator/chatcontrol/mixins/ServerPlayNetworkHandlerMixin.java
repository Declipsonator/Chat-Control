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
                ChatControl.LOG.info("Filtered message from " + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());
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
                if(Config.tellPlayer) sender.sendMessage(Text.of("Your message was censored by Chat Control"));
                if(Config.logFiltered) ChatControl.LOG.info("Censored message from " + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());
                return SignedMessage.ofUnsigned(newMessage);
            }
        }
        return message;
    }


}
