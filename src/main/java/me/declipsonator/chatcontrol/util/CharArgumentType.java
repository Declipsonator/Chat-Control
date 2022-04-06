package me.declipsonator.chatcontrol.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.declipsonator.chatcontrol.ChatControl;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CharArgumentType implements ArgumentType<Character> {
    public static CharArgumentType character() {
        return new CharArgumentType();
    }


    private static final Collection<String> EXAMPLES = new ArrayList<>(List.of("a", "t", "@"));

    @Override
    public Character parse(StringReader reader) throws CommandSyntaxException {
        int argBeginning = reader.getCursor();
        if (!reader.canRead()) {
            reader.skip();
        }
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }

        String charString = reader.getString().substring(argBeginning, reader.getCursor());


        if(charString.length() != 1) {
            throw new SimpleCommandExceptionType(Text.of("Char requires 1 character")).createWithContext(reader);
        }
        return charString.charAt(0);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}


