package me.nik.superenchantments.utils;

import org.bukkit.entity.Player;

public final class ExpUtils {

    private ExpUtils() {
    }

    public static boolean isLevels(String type) {
        return !type.equalsIgnoreCase("POINTS");
    }

    public static int pointsToNext(int level) {

        if (level >= 31) return 9 * level - 158;

        if (level >= 16) return 5 * level - 38;

        return 2 * level + 7;
    }

    public static int pointsForLevel(int level) {

        if (level >= 32) return (int) (4.5D * level * level - 162.5D * level + 2220);

        if (level >= 17) return (int) (2.5D * level * level - 40.5D * level + 360);

        return level * level + 6 * level;
    }

    public static int getTotal(Player player) {

        int level = player.getLevel();

        return pointsForLevel(level) + Math.round(player.getExp() * pointsToNext(level));
    }

    public static void setTotal(Player player, int total) {

        int remaining = Math.max(0, total);

        int level = 0;

        while (remaining >= pointsToNext(level)) {
            remaining -= pointsToNext(level);
            level++;
        }

        player.setLevel(level);
        player.setExp((float) remaining / pointsToNext(level));
    }

    public static boolean has(Player player, boolean levels, int amount) {
        return levels ? player.getLevel() >= amount : getTotal(player) >= amount;
    }

    public static void take(Player player, boolean levels, int amount) {

        if (amount <= 0) return;

        if (levels) {
            player.setLevel(player.getLevel() - amount);
        } else {
            setTotal(player, getTotal(player) - amount);
        }
    }
}