package me.nik.superenchantments;

import org.bukkit.ChatColor;

public final class Text {

    private Text() {}

    public static String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String strip(String s) {
        return s == null ? "" : ChatColor.stripColor(s);
    }
}
