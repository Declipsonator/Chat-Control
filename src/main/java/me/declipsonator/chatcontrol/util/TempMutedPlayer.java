package me.declipsonator.chatcontrol.util;

import java.util.UUID;

public record TempMutedPlayer(UUID uuid, long until, String reason) {

    @Override
    public String toString() {
        return "{uuid: " + uuid + " until: " + until + " reason: " + reason + "}";
    }
}
