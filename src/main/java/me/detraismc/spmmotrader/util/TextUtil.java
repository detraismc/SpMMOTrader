package me.detraismc.spmmotrader.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#+([0-9a-fA-F]{6})>");

    private TextUtil() {}

    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        String legacy = text.replace('&', '\u00A7');
        Matcher matcher = HEX_PATTERN.matcher(legacy);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append('\u00A7').append(c);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return LegacyComponentSerializer.legacySection().deserialize(sb.toString())
            .decoration(TextDecoration.ITALIC, false);
    }

    public static String formatNumber(int number) {
        if (number == 0) return "0";
        StringBuilder sb = new StringBuilder();
        String num = String.valueOf(number);
        int count = 0;
        for (int i = num.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                sb.append(' ');
            }
            sb.append(num.charAt(i));
            count++;
        }
        return sb.reverse().toString();
    }

    public static String legacyColorize(String text) {
        if (text == null) return "";
        text = text.replace('&', '\u00A7');
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("\u00A7x");
            for (char c : hex.toCharArray()) {
                replacement.append('\u00A7').append(c);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
