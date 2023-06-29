package me.declipsonator.chatcontrol.mixins;

import me.declipsonator.chatcontrol.ChatControl;
import me.declipsonator.chatcontrol.util.Config;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChatMessage(SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params, CallbackInfo ci) {
        ServerPlayerEntity sender = (ServerPlayerEntity) (Object) this;
        if(sender == null || Config.isIgnored(sender.getUuid()) || Config.ignorePrivateMessages) return;
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

    @ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private SentMessage onBroadcastChatMessage(SentMessage message, SentMessage m, boolean filterMaskEnabled, MessageType.Parameters params) {
        ServerPlayerEntity sender = (ServerPlayerEntity) (Object) this;
        if(Config.ignorePrivateMessages || Config.isIgnored(sender.getUuid())) return message;
        if(!Config.isMuted(sender.getUuid()) && Config.censorAndSend) {
            String newMessage = Config.censorWords(message.getContent().getString());
            newMessage = Config.censorPhrases(newMessage);
            newMessage = Config.censorRegexes(newMessage);
            newMessage = Config.censorStandAloneWords(newMessage);
            if(!newMessage.equals(message.getContent().getString())) {
                if(Config.tellPlayer) sender.sendMessage(Text.of("Your message was censored by Chat Control"));
                if(Config.logFiltered) ChatControl.LOG.info("Censored message from " + sender.getDisplayName().getString() + " (" + sender.getUuid().toString() + ")" + ": " + message.getContent().getString());

                if(message instanceof SentMessage.Profileless) {
                    return new SentMessage.Profileless(Text.of(newMessage));
                }

//                SignedMessage sMessage = ((SentMessage.Chat) message).message();

                return SentMessage.of(SignedMessage.ofUnsigned(newMessage));
//                return SentMessage.of(new SignedMessage(sMessage.link(), sMessage.signature(),
//                        new MessageBody(newMessage, sMessage.signedBody().timestamp(), sMessage.signedBody().salt(), sMessage.signedBody().lastSeenMessages()),
//                        Text.of(newMessage), sMessage.filterMask()));
            }

        }
        return message;
    }
}
