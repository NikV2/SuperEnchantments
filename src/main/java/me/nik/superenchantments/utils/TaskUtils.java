package me.nik.superenchantments.utils;

import me.nik.superenchantments.SuperEnchantments;
import org.bukkit.Bukkit;

public final class TaskUtils {

    private TaskUtils() {
    }

    public static void taskTimer(Runnable runnable, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimer(SuperEnchantments.getInstance(), runnable, delay, interval);
    }

    public static void taskTimerAsync(Runnable runnable, long delay, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SuperEnchantments.getInstance(), runnable, delay, interval);
    }

    public static void task(Runnable runnable) {
        Bukkit.getScheduler().runTask(SuperEnchantments.getInstance(), runnable);
    }

    public static void taskAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(SuperEnchantments.getInstance(), runnable);
    }

    public static void taskLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(SuperEnchantments.getInstance(), runnable, delay);
    }

    public static void taskLaterAsync(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(SuperEnchantments.getInstance(), runnable, delay);
    }
}