package me.declipsonator.chatcontrol.util;

import java.util.UUID;

public record TempMutedPlayer(UUID uuid, long until) {

    @Override
    public String toString() {
        return "{uuid: " + uuid + " until: " + until + "}";
    }
}
