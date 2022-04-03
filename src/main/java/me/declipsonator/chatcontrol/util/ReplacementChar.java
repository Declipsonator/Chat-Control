package me.declipsonator.chatcontrol.util;

public class ReplacementChar {
    public final char toReplace;
    public final char replaceWith;

    public ReplacementChar(char toReplace, char replaceWith) {
        this.toReplace = toReplace;
        this.replaceWith = replaceWith;
    }

    @Override
    public String toString() {
        return "{" + "toReplace: " + toReplace + ", replaceWith: " + replaceWith + '}';
    }
}
