package me.declipsonator.chatcontrol.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;


// This is to map the old format of muted players to the new format with reasons
public class MutedPlayerTypeAdapter extends TypeAdapter<MutedPlayer> {

    @Override
    public void write(JsonWriter out, MutedPlayer value) throws IOException {
        out.beginObject();
        out.name("uuid").value(value.uuid().toString());
        out.name("reason").value(value.reason());
        out.endObject();
    }

    @Override
    public MutedPlayer read(JsonReader in) throws IOException {
        UUID uuid = null;
        String reason = null;

        if (in.peek() == JsonToken.STRING) {
            uuid = UUID.fromString(in.nextString());
            reason = "No reason provided"; // default reason for old format
        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "uuid":
                        uuid = UUID.fromString(in.nextString());
                        break;
                    case "reason":
                        reason = in.nextString();
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
        }

        if (uuid == null) {
            throw new IOException("Missing uuid");
        }

        return new MutedPlayer(uuid, reason);
    }
}