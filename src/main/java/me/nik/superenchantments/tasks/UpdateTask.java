package me.nik.superenchantments.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class UpdateTask extends BukkitRunnable {

    private static final List<Runnable> updates = new ArrayList<>();

    @Override
    public void run() {
        updates.forEach(Runnable::run);
    }

    public static void add(Runnable runnable) {
        updates.add(runnable);
    }
}