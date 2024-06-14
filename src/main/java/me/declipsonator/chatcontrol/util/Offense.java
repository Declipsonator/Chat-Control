package me.declipsonator.chatcontrol.util;

import java.util.UUID;

public record Offense(UUID uuid, long time) {

        @Override
        public String toString() {
            return "{uuid: " + uuid + " time: " + time + "}";
        }
}
