package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SniperEnchant extends CustomEnchant {
    public SniperEnchant(SuperEnchantments plugin) {
        super(plugin, "sniper", "Sniper", "&a", "BOW");

        addOption("distance_multiplier", 0.5, "Drop experience from the extra blocks");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBossAttack(EntityDamageByEntityEvent e) {

        Entity target = e.getEntity();
        Entity damager = e.getDamager();

        if (target == null || damager == null || !(damager instanceof Arrow)) return;

        Arrow arrow = (Arrow) damager;

        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();

        Location playerLocation = player.getLocation().clone();
        Location targetLocation = target.getLocation().clone();

        playerLocation.setY(0);
        targetLocation.setY(0);

        double distance = playerLocation.distance(targetLocation) * Math.min(1.0, getDouble("distance_multiplier"));

        e.setDamage(e.getDamage() + distance);
    }
}