package me.nik.superenchantments.utils;

import me.nik.superenchantments.SuperEnchantments;
import org.bukkit.ChatColor;

import java.util.regex.Pattern;

public final class ChatUtils {

    private ChatUtils() {
    }

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '&' + "[0-9A-FK-OR]");

    public static String format(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String stripColorCodes(final String input) {
        return ChatColor.stripColor(STRIP_COLOR_PATTERN.matcher(input).replaceAll(""));
    }

    public static String plain(final String input) {

        StringBuilder builder = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            if ((c == '&' || c == '\u00a7') && i + 1 < input.length() && "0123456789abcdefklmnorxABCDEFKLMNORX".indexOf(input.charAt(i + 1)) != -1) {
                i++;
                continue;
            }

            builder.append(c);
        }

        return builder.toString().trim();
    }

    public static void log(final String... messages) {
        for (String msg : messages) {
            if (msg != null) {
                SuperEnchantments.getInstance().getLogger().info(msg);
            }
        }
    }
}