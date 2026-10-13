package me.nik.superenchantments.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UpdateTask extends BukkitRunnable {

    private final List<Consumer<Player>> updaters = new ArrayList<>();

    @Override
    public void run() {

        if (updaters.isEmpty()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Consumer<Player> updater : updaters) updater.accept(player);
        }
    }

    public void add(Consumer<Player> updater) {
        updaters.add(updater);
    }

    public void clear() {
        updaters.clear();
    }
}
